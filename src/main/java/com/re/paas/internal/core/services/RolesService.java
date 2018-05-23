package com.re.paas.internal.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.reflect.TypeToken;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.fusion.CacheAdapter;
import com.re.paas.internal.core.fusion.FusionHelper;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.FusionEndpoint;
import com.re.paas.internal.core.keys.CacheKeys;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.core.users.RoleUpdateAction;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class RolesService extends BaseService {
@Override
public String uri() {
	return "/roles";
}
	@FusionEndpoint(uri = "/new-role", bodyParams = { "roleName",
			"realm" }, method = HttpMethod.PUT, functionality = Functionality.MANAGE_ROLES)
	public void newRole(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String roleName = body.getString("roleName");
		RoleRealm roleRealm = RoleRealm.from(body.getInteger("realm"));

		RoleModel.newRole(roleName, roleRealm);
	}

	@FusionEndpoint(uri = "/role", requestParams = {
			"roleName" }, method = HttpMethod.DELETE, functionality = Functionality.MANAGE_ROLES)
	public void deleteRole(RoutingContext ctx) {
		String roleName = ctx.request().getParam("roleName");
		RoleModel.deleteRole(roleName);
	}

	@FusionEndpoint(uri = "/list", requestParams = {
			"realm" }, method = HttpMethod.GET, functionality = Functionality.LIST_ROLES)
	public void listRoles(RoutingContext ctx) {

		String realm = ctx.request().getParam("realm");

		Map<String, Integer> roles = realm.equals("undefined") ? RoleModel.listRoles()
				: RoleModel.listRoles(RoleRealm.from(Integer.parseInt(realm)));

		ctx.response().write(GsonFactory.getInstance().toJson(roles)).setChunked(true);
	}

	@FusionEndpoint(uri = "/user-count", bodyParams = {
			"roleNames" }, method = HttpMethod.POST, functionality = Functionality.MANAGE_ROLES)
	public void getUsersCount(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();
		List<String> names = GsonFactory.getInstance().fromJson(body.getJsonArray("roleNames").encode(),
				new TypeToken<List<String>>() {
				}.getType());

		Map<String, Integer> result = RoleModel.getUsersCount(names);
		ctx.response().write(GsonFactory.getInstance().toJson(result)).setChunked(true);
	}

	@FusionEndpoint(uri = "/does-user-role-allow", bodyParams = {
			"functionalities" }, method = HttpMethod.POST, functionality = Functionality.GET_ROLE_FUNCTIONALITIES)
	public void doesUserRoleAllow(RoutingContext ctx) {

		Long principal = FusionHelper.getUserId(ctx.request());

		JsonObject body = ctx.getBodyAsJson();

		List<Integer> functionalities_ = GsonFactory.getInstance()
				.fromJson(body.getJsonArray("functionalities").encode(), new TypeToken<List<Integer>>() {
				}.getType());

		List<Functionality> functionalities = new ArrayList<>();

		functionalities_.forEach(i -> {
			functionalities.add(Functionality.from(i));
		});

		String roleName = BaseUserModel.getRole(principal);

		Boolean isAllowed = RoleModel.isAccessAllowed(roleName,
				functionalities.toArray(new Functionality[functionalities.size()]));
		ctx.response().write(GsonFactory.getInstance().toJson(isAllowed)).setChunked(true);
	}

	@FusionEndpoint(uri = "/realms", method = HttpMethod.GET, functionality = Functionality.GET_ROLE_REALMS)
	public void listRealms(RoutingContext ctx) {
		Map<String, Integer> roles = RoleModel.listRoles();
		ctx.response().write(GsonFactory.getInstance().toJson(roles)).setChunked(true);
	}

	/**
	 * This retrieves all the functionalities applicable to this role realm
	 */
	@FusionEndpoint(uri = "/realm-functionalities", requestParams = {
			"realm" }, functionality = Functionality.GET_REALM_FUNCTIONALITIES)
	public void getRealmFunctionalities(RoutingContext ctx) {
		RoleRealm roleRealm = RoleRealm.from(Integer.parseInt(ctx.request().getParam("realm")));
		String json = GsonFactory.getInstance().toJson(RoleModel.getRealmFunctionalities(roleRealm));
		ctx.response().setChunked(true).write(json);
	}

	/**
	 * This retrieves all the functionalities for this role
	 */
	@FusionEndpoint(uri = "/functionalities", requestParams = {
			"roleName" }, functionality = Functionality.GET_ROLE_FUNCTIONALITIES)
	public void getRoleFunctionalities(RoutingContext ctx) {

		String roleName = ctx.request().getParam("roleName");
		List<Integer> e = RoleModel.getRoleFunctionalities(roleName);

		CacheAdapter.put(CacheKeys.ROLE_FUNCTIONALITIES_$ROLE.replace("$ROLE", roleName), new JsonArray(e).toString());

		String json = GsonFactory.getInstance().toJson(e);
		ctx.response().setChunked(true).write(json);
	}

	@FusionEndpoint(uri = "/update-spec", bodyParams = { "roleName", "functionality",
			"action" }, method = HttpMethod.POST, functionality = Functionality.MANAGE_ROLES)
	public void updateRoleSpec(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String roleName = body.getString("roleName");
		Integer functionality = Integer.parseInt(body.getString("functionality"));
		RoleUpdateAction updateAction = RoleUpdateAction.from(body.getInteger("action"));

		RoleModel.updateRoleSpec(roleName, updateAction, functionality);

		CacheAdapter.del(CacheKeys.ROLE_FUNCTIONALITIES_$ROLE.replace("$ROLE", roleName));
	}

	@FusionEndpoint(uri = "/default-role", requestParams = { "realm" }, functionality = Functionality.MANAGE_ROLES)
	public void getDefaultRole(RoutingContext ctx) {
		RoleRealm roleRealm = RoleRealm.from(Integer.parseInt(ctx.request().getParam("realm")));
		String role = RoleModel.getDefaultRole(roleRealm);
		ctx.response().setChunked(true).write(role);
	}

	@FusionEndpoint(uri = "/get-role-realm", requestParams = { "role" }, functionality = Functionality.GET_ROLE_REALMS)
	public void getRoleRealm(RoutingContext ctx) {
		String roleName = ctx.request().getParam("role");
		RoleRealm realm = RoleModel.getRealm(roleName);
		ctx.response().setChunked(true).write(GsonFactory.getInstance().toJson(realm));
	}

}
