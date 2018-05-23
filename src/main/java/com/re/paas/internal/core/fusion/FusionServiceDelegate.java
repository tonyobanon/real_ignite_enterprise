package com.re.paas.internal.core.fusion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.re.paas.gae_adapter.core.fusion.ErrorHelper;
import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.base.classes.ObjectWrapper;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.ResourceException;
import com.re.paas.internal.base.core.ThreadContext;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.FusionEndpoint;
import com.re.paas.internal.core.fusion.api.Route;
import com.re.paas.internal.core.fusion.api.ServiceDelegate;
import com.re.paas.internal.core.services.ResourceBundleService;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.LocaleModel;
import com.re.paas.internal.spi.DependsOn;
import com.re.paas.internal.spi.SpiTypes;
import com.re.paas.internal.utils.ClassUtils;
import com.re.paas.internal.utils.LocaleUtils;
import com.re.paas.internal.utils.Utils;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;

@BlockerTodo("Create optionalRequestParams setting, Validate request params in fusion. Do in main ctx handler. Add support for service docs")
/**
 * Note: This class calls LocaleModel, to set user locale
 */

@DependsOn(SpiTypes.CLOUD_ENVIRONMENT)
public class FusionServiceDelegate extends ServiceDelegate {

	private static final String FUSION_CLIENT_PATH = System.getProperty("java.io.tmpdir") + File.separator
			+ AppDelegate.getPlatformPrefix() + File.separator + "fusion-service-clients" + File.separator;

	public static Pattern endpointClassUriPattern = Pattern
			.compile("\\A\\Q/\\E[a-zA-Z]+[-]*[a-zA-Z]+(\\Q/\\E[a-zA-Z]+[-]*[a-zA-Z]+)*\\z");

	public static Pattern endpointMethodUriPattern = Pattern.compile("\\A\\Q/\\E[a-zA-Z-]+[-]*[a-zA-Z]+\\z");

	private static final String ROUTE_HANDLER_RK_PREFIX = "rhrkp_";
	private static final String ROUTE_HANDLER_KEYS = "rhk";

	private static final String ROUTE_FUNCTIONALITY_RK_PREFIX = "rfrkp_";
	private static final String FUNCTIONALITY_ROUTES_RK_PREFIX = "rfrkp_";

	protected static final String USER_ID_PARAM_NAME = "x_uid";
	public static final String BASE_PATH = "/api";

	@Override
	public Multimap<Route, RouteHandler> getRouteHandlers() {

		List<String> routes = getList(String.class, ROUTE_HANDLER_KEYS);

		Multimap<Route, RouteHandler> result = LinkedHashMultimap.create(routes.size(), 2);

		routes.forEach(r -> {
			Route route = Route.fromString(r);
			getList(RouteHandler.class, ROUTE_HANDLER_RK_PREFIX + route).forEach(h -> {
				result.put(route, h);
			});
		});

		return result;
	}

	@Override
	public List<RouteHandler> getRouteHandlers(Route route) {
		String namespace = (ROUTE_HANDLER_RK_PREFIX + route.toString());
		return getList(RouteHandler.class, namespace);
	}

	private void addRouteHandler(Route route, RouteHandler handler) {
		String namespace = ROUTE_HANDLER_RK_PREFIX + route.toString();
		addToList(namespace, handler);
		addToList(ROUTE_HANDLER_KEYS, route.toString());
	}

	@Override
	public Functionality getRouteFunctionality(Route route) {
		String namespace = (ROUTE_FUNCTIONALITY_RK_PREFIX + route.toString());
		Integer functionalityId = (Integer) get(namespace);
		return Functionality.from(functionalityId);
	}

	private void setRouteFunctionality(String route, Integer functionalityId) {
		String namespace = ROUTE_FUNCTIONALITY_RK_PREFIX + route;
		if (!hasKey(namespace)) {
			set(namespace, functionalityId);
		} else {
			Exceptions.throwRuntime("Route: " + route + " already exists");
		}
	}

	@Override
	public List<String> getFunctionalityRoute(Functionality functionality) {
		String namespace = (FUNCTIONALITY_ROUTES_RK_PREFIX + functionality.getId());
		return getList(String.class, namespace);
	}

	private void addFunctionalityRoute(Integer functionalityId, String route) {
		String namespace = FUNCTIONALITY_ROUTES_RK_PREFIX + functionalityId;
		addToList(namespace, route);
	}

	@Override
	protected void init() {

		CacheAdapter.start();

		Logger.get().debug("Scanning for API routes");

		addRouteHandler(new Route(), defaultRouteHandler());

		// Then, add fusion services found in classpath

		ObjectWrapper<StringBuilder> serviceCientBuffer = new ObjectWrapper<StringBuilder>().set(new StringBuilder());

		// This is used to avoid duplicate service methods, since they all exists in a
		// global client context
		Map<String, String> methodNames = new HashMap<>();

		scanAll(context -> {

			String className = context.getService().getClass().getSimpleName();
			String methodName = context.getMethod().getName();

			if (methodNames.containsKey(methodName)) {

				String msg = "Method name: " + methodName + "(..) in " + className + " already exists in "
						+ methodNames.get(methodName);
				throw new ResourceException(ResourceException.RESOURCE_ALREADY_EXISTS, msg);
			}

			methodNames.put(methodName, className);

			Functionality functionality = context.getEndpoint().functionality();

			String uri = context.getService().uri() + context.getEndpoint().uri();
			HttpMethod httpMethod = context.getEndpoint().method();

			Route route = new Route(uri, httpMethod);

			Logger.get().trace("Mapping route: " + uri + " (" + httpMethod + ") to functionality: " + functionality);

			setRouteFunctionality(route.toString(), context.getEndpoint().functionality().getId());

			addFunctionalityRoute(functionality.getId(), uri);

			if (context.getEndpoint().createXhrClient()) {
				// Generate XHR clients
				serviceCientBuffer.get().append(RPCFactory.generateXHRClient(context.getService(), context.getMethod(),
						context.getEndpoint(), route));
			}

			if (context.getEndpoint().customAuthenticator() != DefaultServiceAuthenticator.class) {
				// Register custom authenticator
				Handlers.addCustomAuthenticator(uri,
						ClassUtils.createInstance(context.getEndpoint().customAuthenticator()));
			}

			// Add Handler

			RouteHandler handler = new RouteHandler(((ctx) -> {

				// Verify Scheme
				if (context.getEndpoint().requireSSL()) {
					if (!ctx.request().isSSL()) {
						ctx.response().setStatusCode(HttpServletResponse.SC_NOT_ACCEPTABLE).end();
					}
				}

				if (context.getEndpoint().cache()) {
					// allow proxies to cache the data
					ctx.response().putHeader("Cache-Control", "public, max-age=" + WebRoutes.DEFAULT_CACHE_MAX_AGE);
				} else {
					ctx.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
				}

				try {
					context.getMethod().invoke(context.getService(), ctx);
				} catch (Exception e) {
					ctx.response().setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
							.end(com.re.paas.internal.core.fusion.Utils.toResponse(ErrorHelper.getError(e)));
				}

			}), context.getEndpoint().isBlocking());

			addRouteHandler(route, handler);

			// Generate Javascript client

			if (context.isClassEnd() && !CloudEnvironment.get().isProduction()) {
				saveServiceClient(serviceCientBuffer.get().toString(), context.getService());
				serviceCientBuffer.set(new StringBuilder());
			}

		});

		// All routes have been added, Start internal server

		if (CloudEnvironment.get().isStandalone()) {

			Logger.get().info("Launching fusion web server ..");

			ServerOptions options = new ServerOptions().withHost(CloudEnvironment.get().httpHost())
					.withPort(CloudEnvironment.get().httpPort());

			WebServer.start(options, this);
		}
	}

	private static RouteHandler defaultRouteHandler() {
		return new RouteHandler(ctx -> {

			// Note: Vertx pools request threads, we need to create new LocalThread context
			ThreadContext.newRequestContext();

			// Auth Handler
			Handlers.APIAuthHandler(ctx);

			// Detect User Locale
			Cookie localeCookie = ctx.getCookie(ResourceBundleService.DEFAULT_LOCALE_COOKIE);
			List<String> locales = new ArrayList<>();

			if (localeCookie != null) {
				locales.add(localeCookie.getValue());
			} else {
				ctx.acceptableLanguages().forEach(lh -> {
					locales.add(lh.value().replaceFirst(Pattern.quote("_"), LocaleUtils.LANGUAGE_COUNTRY_DELIMETER));
				});
			}
			LocaleModel.setUserLocale(locales, FusionHelper.getUserId(ctx.request()));

		}, false);
	}

	/**
	 * This discovers fusion services by scanning the classpath
	 */
	private void scanAll(Consumer<FusionServiceContext> consumer) {

		Logger.get().debug("Scanning for services");

		get(c -> {

			final BaseService service = ClassUtils.createInstance(c);

			if (!endpointClassUriPattern.matcher(service.uri()).matches()) {
				throw new RuntimeException("Improper URI format for " + c.getName());
			}

			List<Method> methodsList = new ArrayList<Method>();

			for (Method m : c.getDeclaredMethods()) {

				// Note: Lambda functions are compiled as synthetic members of the declaring
				// class, and some private helper methods may be contained in Service classes

				if (!m.isSynthetic() && m.getAnnotation(FusionEndpoint.class) != null) {
					methodsList.add(m);
				}
			}

			Method[] methods = methodsList.toArray(new Method[methodsList.size()]);

			for (int i = 0; i < methods.length; i++) {

				Method method = methods[i];

				if (!method.isAnnotationPresent(FusionEndpoint.class)) {
					// Silently ignore
					continue;
				}

				FusionEndpoint endpoint = method.getAnnotation(FusionEndpoint.class);

				if (!endpointMethodUriPattern.matcher(endpoint.uri()).matches()) {
					throw new RuntimeException("Improper URI format for " + c.getName() + "/" + method.getName());
				}

				consumer.accept(new FusionServiceContext(service, endpoint, method, i == methods.length - 1));
			}
		});
	}

	private static void saveServiceClient(String buffer, BaseService service) {

		String name = service.getClass().getSimpleName();

		String path = FUSION_CLIENT_PATH + name.replace("Service", "").toLowerCase() + ".js";

		File clientStubFile = new File(path);

		clientStubFile.mkdirs();
		try {
			if (clientStubFile.exists()) {
				clientStubFile.delete();
			}

			clientStubFile.createNewFile();

			Logger.get().trace("Saving service client for " + name + " to " + clientStubFile.getAbsolutePath());

			Utils.saveString(buffer, Files.newOutputStream(clientStubFile.toPath()));

		} catch (IOException e) {
			Exceptions.throwRuntime(e);
		}
	}

	protected Router getRouter() {

		final Router router = Router.router(WebServer.vertX);

		getRouteHandlers().entries().forEach((e) -> {
			addRoute(router, e.getKey(), e.getValue());
		});

		router.route().handler(ctx -> {
			if (!ctx.response().ended()) {

				if (ctx.response().bytesWritten() == 0) {
					ctx.response()
							.write(com.re.paas.internal.core.fusion.Utils.toResponse(ctx.response().getStatusCode()));
				}
				ctx.response().end();
			}
		});
		return router;
	}

	private static final void addRoute(Router router, Route route, RouteHandler handler) {

		io.vertx.ext.web.Route r =

				// Match all paths and methods
				route.getMethod() == null && route.getUri() == null ? router.route() :
				// Match by method only
						route.getMethod() != null && route.getUri() == null
								? router.routeWithRegex(route.getMethod(), "/*")
								:
								// Match by path only
								route.getUri() != null && route.getMethod() == null ? router.route(route.getUri()) :
								// Match by method and path
										router.route(route.getMethod(), route.getUri());

		if (handler.isBlocking()) {
			r.blockingHandler(handler.getHandler());
		} else {
			r.handler(handler.getHandler());
		}

	}

	static {
		RPCFactory.setPrependDomainVariableToUrl(false);
	}
}
