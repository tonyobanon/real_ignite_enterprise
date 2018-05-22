package com.re.paas.gae_adapter.requests.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.re.paas.internal.core.fusion.APIRoutes;
import com.re.paas.internal.core.fusion.Route;
import com.re.paas.internal.core.fusion.RouteHandler;

import io.vertx.core.http.HttpMethod;

@WebServlet(urlPatterns = APIRoutes.BASE_PATH + "/*")
public class BaseApiServlet extends BaseServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		// Mock RoutingContext from HttpServletRequest

		com.re.paas.gae_adapter.core.fusion.GAERouteContext ctx = new com.re.paas.gae_adapter.core.fusion.GAERouteContext(
				req);

		String path = ctx.request().path().replace(APIRoutes.BASE_PATH, "");

		HttpMethod method = ctx.request().method();

		// Find all matching handlers

		List<RouteHandler> handlers = new ArrayList<>();

		// Matching all paths and methods
		handlers.addAll(APIRoutes.getRouteHandler(new Route()));

		// Matching only current method
		handlers.addAll(APIRoutes.getRouteHandler(new Route().setMethod(method)));

		// Matching only current path
		handlers.addAll(APIRoutes.getRouteHandler(new Route().setUri(path)));

		// Matching current path and method
		handlers.addAll(APIRoutes.getRouteHandler(new Route().setMethod(method).setUri(path)));

		// Recursively call each handler

		for (RouteHandler handler : handlers) {

			// Create exception catching mechanism

			try {
				handler.getHandler().handle(ctx);

			} catch (Exception e) {
				ctx.response().setStatusCode(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
						.end(com.re.paas.internal.core.fusion.Utils
								.toResponse(com.re.paas.gae_adapter.core.fusion.ErrorHelper.getError(e)));
				break;
			}

			// Check if response is ended

			if (ctx.response().ended()) {
				break;
			}
		}

		if (!ctx.response().ended()) {
			if (ctx.response().bytesWritten() == 0) {
				ctx.response().write(com.re.paas.internal.core.fusion.Utils.toResponse(ctx.response().getStatusCode()));
			}
			ctx.response().end();
		}

		ctx.response().transform(resp);
		resp.flushBuffer();
	}
}
