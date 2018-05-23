package com.re.paas.internal.core.fusion;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.Route;
import com.re.paas.internal.core.fusion.api.ServiceAuthenticator;
import com.re.paas.internal.core.users.Functionality;

import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

public class Handlers {

	private static Map<String, ServiceAuthenticator> customAuthenticators = new HashMap<>();

	public static void APIAuthHandler(RoutingContext ctx) {

		String path = ctx.request().path().replace(FusionServiceDelegate.BASE_PATH, "");

		ServiceAuthenticator authenticator = customAuthenticators.get(path);

		if (authenticator != null) {

			if (authenticator.authenticate(ctx)) {
				ctx.next();
			} else {
				ctx.response().setStatusCode(HttpServletResponse.SC_UNAUTHORIZED)
						.write(com.re.paas.internal.core.fusion.Utils.toResponse(HttpServletResponse.SC_UNAUTHORIZED))
						.end();
			}
			return;
		}

		// set json response type
		ctx.response().putHeader("Content-Type", "application/json");

		// do not allow proxies to cache the data
		ctx.response().putHeader("Cache-Control", "no-store, no-cache");

		String uri = ctx.request().path().replace(FusionServiceDelegate.BASE_PATH, "");
		HttpMethod method = ctx.request().method();

		Route route = new Route(uri, method);
		Functionality functionality = BaseService.getDelegate().getRouteFunctionality(route);

		if (functionality == null || functionality.getId() < 0) {
			ctx.next();
			return;
		}

		boolean hasAccess = false;

		// Get sessionToken from either a cookie or request header
		String sessionToken;
		try {
			sessionToken = ctx.getCookie(FusionHelper.sessionTokenName()).getValue();
		} catch (NullPointerException e) {
			sessionToken = ctx.request().getHeader(FusionHelper.sessionTokenName());
		}

		Long userId = FusionHelper.getUserIdFromToken(sessionToken);

		if (userId != null) {
			hasAccess = true;
		}

		if (functionality.getId() > 0 && hasAccess == true) {
			hasAccess = false;
			for (String roleName : FusionHelper.getRoles(userId)) {

				// Check that this role has the right to access this Uri
				if (FusionHelper.isAccessAllowed(roleName, functionality.getId())) {
					hasAccess = true;
					break;
				}
			}
		}

		if (hasAccess) {
			FusionHelper.setUserId(ctx.request(), userId);
			ctx.next();
		} else {
			ctx.response().setStatusCode(HttpServletResponse.SC_UNAUTHORIZED)
					.write(com.re.paas.internal.core.fusion.Utils.toResponse(HttpServletResponse.SC_UNAUTHORIZED))
					.end();
		}

	}

	public static void addCustomAuthenticator(String uri, ServiceAuthenticator customAuthenticator) {
		Handlers.customAuthenticators.put(uri, customAuthenticator);
	}

}
