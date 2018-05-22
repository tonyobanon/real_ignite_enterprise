package com.re.paas.internal.core.fusion;

import java.util.List;

import com.google.gson.reflect.TypeToken;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.base.core.Note;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.core.keys.CacheKeys;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class FusionHelper {

	public static Long getUserIdFromToken(String sessionToken) {

		if (sessionToken == null) {
			return null;
		}

		return (Long) CacheAdapter.get(CacheKeys.SESSION_TOKEN_TO_USER_ID_$TOKEN.replace("$TOKEN", sessionToken));
	}

	public static Long getUserId(HttpServerRequest req) {
		String userId = req.getParam(APIRoutes.USER_ID_PARAM_NAME);
		return userId != null ? Long.parseLong(userId) : null;
	}

	public static void setUserId(HttpServerRequest req, Long userId) {
		req.params().add(APIRoutes.USER_ID_PARAM_NAME, userId.toString());

	}

	public static List<String> getRoles(Long userId) {
		return new FluentArrayList<String>().with(BaseUserModel.getRole(userId));
	}

	public static final String sessionTokenName() {
		return "X-Session-Token";
	}

	public static List<Integer> functionalities(RoutingContext ctx) {

		// Get sessionToken from either a cookie or request header
		String sessionToken;
		try {
			sessionToken = ctx.getCookie(FusionHelper.sessionTokenName()).getValue();
		} catch (NullPointerException e) {
			sessionToken = ctx.request().getHeader(FusionHelper.sessionTokenName());
		}

		Long userId = FusionHelper.getUserIdFromToken(sessionToken);

		String roleName = FusionHelper.getRoles(userId).get(0);

		// Check that this role has the right to view this page

		List<Integer> functionalities = FusionHelper.getCachedFunctionalities(roleName);

		if (functionalities == null) {

			functionalities = FusionHelper.getFunctionalities(roleName);

			// Cache role functionalities
			FusionHelper.cacheFunctionalities(roleName, functionalities);
		}

		return functionalities;
	}

	@Note
	public static boolean isAccessAllowed(String roleName, Integer functionalityId) {

		// Check that this role has the right to view this page

		//List<Integer> functionalities = FusionHelper.getCachedFunctionalities(roleName);
		
		List<Integer> functionalities = FusionHelper.getFunctionalities(roleName);

		if (functionalities == null) {

			functionalities = FusionHelper.getFunctionalities(roleName);

			// Cache role functionalities
			FusionHelper.cacheFunctionalities(roleName, functionalities);
		}

		return (functionalities.contains(functionalityId));
	}

	public static void cacheFunctionalities(String roleName, List<Integer> functionalities) {
		CacheAdapter.put(CacheKeys.ROLE_FUNCTIONALITIES_$ROLE.replace("$ROLE", roleName),
				new JsonArray(functionalities).toString());
	}

	public static List<Integer> getCachedFunctionalities(String roleName) {
		Object o = CacheAdapter.get(CacheKeys.ROLE_FUNCTIONALITIES_$ROLE.replace("$ROLE", roleName));
		if (o != null) {
			
			List<Integer> v = GsonFactory.getInstance()
					.fromJson(o.toString(), new TypeToken<List<Integer>>() {
					}.getType());
			
			Logger.get().trace("Getting cached functionalities for role: " + roleName + ": " + v);
			return v;
		}
		return null;
	}

	private static List<Integer> getFunctionalities(String roleName) {
		return RoleModel.getRoleFunctionalities(roleName);
	}
	
	public static void response(RoutingContext ctx, Object data) {
		ctx.response().write(GsonFactory.getInstance().toJson(data)).setChunked(true);
	}
	
	public static void response(RoutingContext ctx, String data) {
		ctx.response().write(data).setChunked(true);
	}
	
	/**
	 * If the response is just a Number, this will cause the client side javascript promise to fail
	 * */
	public static void response(RoutingContext ctx, Number data) {
		ctx.response().write(new JsonObject().put("data", data).encode()).setChunked(true);
	}
}
