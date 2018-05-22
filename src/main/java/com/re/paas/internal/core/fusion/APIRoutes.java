package com.re.paas.internal.core.fusion;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ArrayListMultimap;
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
import com.re.paas.internal.core.services.ResourceBundleService;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.LocaleModel;
import com.re.paas.internal.spi.SpiDelegate;
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
public class APIRoutes extends SpiDelegate<BaseService> {

	private static final String FUSION_CLIENT_PATH = System.getProperty("java.io.tmpdir") + File.separator
			+ AppDelegate.getPlatformPrefix() + File.separator + "fusion-service-clients" + File.separator;

	public static Pattern endpointClassUriPattern = Pattern
			.compile("\\A\\Q/\\E[a-zA-Z]+[-]*[a-zA-Z]+(\\Q/\\E[a-zA-Z]+[-]*[a-zA-Z]+)*\\z");

	public static Pattern endpointMethodUriPattern = Pattern.compile("\\A\\Q/\\E[a-zA-Z-]+[-]*[a-zA-Z]+\\z");

	private static Multimap<String, RouteHandler> routeKeys = LinkedHashMultimap.create();
	private static Multimap<Route, RouteHandler> routes = LinkedHashMultimap.create();

	protected static Map<Object, Integer> routesMappings = new HashMap<>();

	protected static Map<Integer, List<String>> functionalityToRoutesMappings = new HashMap<>();

	protected static final String USER_ID_PARAM_NAME = "x_uid";
	public static final String BASE_PATH = "/api";

	@Override
	protected void init() {

		CacheAdapter.start();

		Logger.get().debug("Scanning for API routes");

		routes = ArrayListMultimap.create();

		registerRoute(new Route(), new RouteHandler(ctx -> {

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

		}, false));

		// Then, add fusion services found in classpath

		ObjectWrapper<StringBuilder> serviceCientBuffer = new ObjectWrapper<StringBuilder>().set(new StringBuilder());

		// This is used to avoid duplicate service methods, since they all exists in a
		// global client context
		Map<String, String> methodNames = new HashMap<>();

		scanServices(context -> {

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

			routesMappings.put(route.toString(), context.getEndpoint().functionality().getId());

			if (!functionalityToRoutesMappings.containsKey(functionality.getId())) {
				functionalityToRoutesMappings.put(functionality.getId(), new ArrayList<>());
			}

			functionalityToRoutesMappings.get(functionality.getId()).add(uri);

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

			registerRoute(route, handler);

			// Generate Javascript client

			if (context.isClassEnd() && !CloudEnvironment.get().isProduction()) {
				saveServiceClient(serviceCientBuffer.get().toString(), context.getService());
				serviceCientBuffer.set(new StringBuilder());
			}

		});
	}

	/**
	 * This discovers fusion services by scanning the classpath
	 */
	private static void scanServices(Consumer<FusionServiceContext> consumer) {

		Logger.get().debug("Scanning for services");

		new SpiDelegate<BaseService>(SpiTypes.SERVICE).get(c -> {

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

	/**
	 * This returns a set of request handlers available for this app by scanning for
	 * fusion services. It also includes an auth handler to authenticate requests.
	 * <br>
	 * In the course of execution, this methods populates
	 * {@code APIRoutes.routesMappings}, and also generates javascript clients for
	 * all fusion endpoints.
	 * 
	 */
	public static void scanRoutes() {

	}

	protected static Router get() {

		final Router router = Router.router(WebServer.vertX);

		getRoutes().entries().forEach((e) -> {
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

	public static void clear() {
		routes = null;
	}

	public static Collection<RouteHandler> getRouteHandler(Route route) {
		return routeKeys.get(route.toString());
	}

	private static final void registerRoute(Route route, RouteHandler handler) {

		// Actually, this check is not necessary, and should be removed, since in
		// practice, different service methods can use the same route signature.
		// This was originally done to ensure code integrity, in initial architectural
		// design
		if (!routeKeys.containsKey(route.toString()) || route.toString().equals("*")) {
			routeKeys.put(route.toString(), handler);
			routes.put(route, handler);
		} else {
			throw new ResourceException(ResourceException.RESOURCE_ALREADY_EXISTS,
					"Route: " + route + " already exists");
		}
	}

	/**
	 * Note: All routes paths returned are not prefixed with
	 * {@code APIRoutes.BASE_PATH}. All callers should consolidate this when setting
	 * up their respective containers. <br>
	 * Also, note that exceptions will possibly be thrown during the execution of
	 * these handlers, and callers should create proper exception catching mechanism
	 * on their containers. <br>
	 * Callers should create mechanisms to properly end the response after all
	 * handlers, have finished execution
	 */
	public static Multimap<Route, RouteHandler> getRoutes() {
		if (routes == null) {
			scanRoutes();
		}
		return routes;
	}

	public static List<String> getUri(Functionality functionality) {
		return functionalityToRoutesMappings.get(functionality.getId());
	}

	static {
		RPCFactory.setPrependDomainVariableToUrl(false);
	}
}
