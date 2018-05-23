package com.re.paas.internal.core.fusion;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Sets;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.Todo;
import com.re.paas.internal.base.logging.Logger;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;

public class WebServer {

	protected static Pattern uriPattern = Pattern.compile("\\A\\Q/\\E[a-zA-Z]+(\\Q/\\E[a-zA-Z]+)*\\z");

	public static Pattern endpointClassUriPattern = Pattern
			.compile("\\A\\Q/\\E[a-zA-Z]+[-]*[a-zA-Z]+(\\Q/\\E[a-zA-Z]+[-]*[a-zA-Z]+)*\\z");

	public static Pattern endpointMethodUriPattern = Pattern.compile("\\A\\Q/\\E[a-zA-Z-]+[-]*[a-zA-Z]+\\z");

	public static final Set<String> allowedHeaders = Sets.newHashSet(new String[] { "X-XSRF-TOKEN" });

	protected static Vertx vertX;

	public static Router router;

	public static HttpServer server;

	public static ServerOptions options;

	/**
	 *
	 * @throws IOException
	 */

	@BlockerTodo("Revisit shutdown Hook to JVM")
	public static void start(ServerOptions options, FusionServiceDelegate delegate) {

		vertX = Vertx.vertx(new VertxOptions().setWorkerPoolSize(100));

		WebServer.options = options;

		setupRouter(delegate);

		// Start Server
		server = vertX.createHttpServer(new HttpServerOptions().setMaxWebsocketFrameSize(1000000)
				.setHost(options.getHost().getHostAddress()).setPort(options.getPort())).requestHandler(req -> {
					router.accept(req);
				}).listen();

		Logger.get().info("Microservice container started successfully..");

		// Add shutdown Hook to JVM
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});
	}

	@Todo("Prevent Ddos attack")
	private static void setupRouter(FusionServiceDelegate delegate) {

		// Initialize Router

		if (router == null) {
			router = Router.router(vertX);
		} else {
			router.clear();
		}

		// Configure CORS
		// router.route().handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET).allowedHeaders(allowedHeaders));

		// Setup Cookies
		router.route().handler(CookieHandler.create());

		// router.route()
		// .handler(SessionHandler.create(LocalSessionStore.create(vertX))
		// @DEV
		// .setCookieHttpOnlyFlag(true).setCookieSecureFlag(true));

		// Prevent CSRF attacks
		// The handler adds a CSRF token to requests which mutate state. In
		// order change the state a (XSRF-TOKEN) cookie is set with a unique
		// token, that is expected to be sent back in a (X-XSRF-TOKEN) header.

		// @TODO enable csrf
		// router.route().handler(CSRFHandler.create(GenericUtils.newSecureRandom()));

		// Body Handler
		router.route().handler(BodyHandler.create()
		// .setBodyLimit(4098)
		);

		// Add API routes
		router.mountSubRouter(FusionServiceDelegate.BASE_PATH, delegate.getRouter());

		// Add Web routes
		router.mountSubRouter("/", WebRoutes.get());

		router.get().failureHandler(ctx -> {
			if (ctx.statusCode() == HttpServletResponse.SC_NOT_FOUND) {
				ctx.reroute("/404");
			} else if (ctx.statusCode() == HttpServletResponse.SC_INTERNAL_SERVER_ERROR) {
				ctx.reroute("/error");
			} else {
				ctx.next();
			}
		});

		// Event Bus
		BridgeOptions options = new BridgeOptions();
		router.route("/eventbus/*")
				.handler(SockJSHandler.create(vertX, new SockJSHandlerOptions().setHeartbeatInterval(2000))
						.bridge(options, new WebBridgeHandler()));
	}

	public static void shutdown() {
		server.close();
		vertX.close();
	}

}
