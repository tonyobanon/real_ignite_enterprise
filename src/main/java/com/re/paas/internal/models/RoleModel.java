package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;
import java.util.Map;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.NotFoundException;
import com.googlecode.objectify.cmd.Query;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.QueryFilter;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.core.users.RoleUpdateAction;
import com.re.paas.internal.entites.BaseUserEntity;
import com.re.paas.internal.entites.UserRoleEntity;
import com.re.paas.internal.errors.RolesError;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.models.helpers.EntityUtils;
import com.re.paas.internal.utils.Utils;

public class RoleModel implements BaseModel {

	@Override
	public String path() {
		return "core/roles";
	}

	@Override
	public void preInstall() {

		// Create default roles
		Logger.get().info("Creating default roles");

		newRole("Admin", true, RoleRealm.ADMIN);
		newRole("Organization Admin", true, RoleRealm.ORGANIZATION_ADMIN);
		newRole("Agent", true, RoleRealm.AGENT);
	}

	@Override
	public void install(InstallOptions options) {

	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static void newRole(String name, RoleRealm realm) {
		newRole(name, false, realm);
	}

	protected static void newRole(String name, Boolean isDefault, RoleRealm realm) {
		Logger.get().info("Creating role: " + name);
		ofy().save().entity(new UserRoleEntity().setName(name).setIsDefault(isDefault).setRealm(realm.getValue())
				.setSpec(realm.spec()).setDateCreated(Dates.now())).now();
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static void deleteRole(String name) {

		if (isRoleInUse(name)) {
			throw new PlatformException(RolesError.ROLE_IN_USE_AND_CANNOT_BE_DELETED);
		}

		if (isDefaultRole(name)) {
			throw new PlatformException(RolesError.DEFAULT_ROLE_CANNOT_BE_DELETED);
		}

		ofy().delete().key(Key.create(UserRoleEntity.class, name)).now();
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static Map<String, Integer> listRoles() {

		Map<String, Integer> result = new FluentHashMap<>();

		ofy().load().type(UserRoleEntity.class).list().forEach(o -> {
			result.put(o.getName(), o.getRealm());
		});

		return result;
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static Map<String, Integer> listRoles(RoleRealm realm) {

		Map<String, Integer> roles = new FluentHashMap<>();

		Query<UserRoleEntity> query = ofy().load().type(UserRoleEntity.class).filter("realm =", realm.getValue());
		for (UserRoleEntity role : query) {

			roles.put(role.getName(), realm.getValue());
		}

		return roles;
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static Map<String, Integer> getUsersCount(List<String> names) {
		Map<String, Integer> result = new FluentHashMap<>();
		names.forEach(name -> {
			Integer count = EntityUtils.query(BaseUserEntity.class, QueryFilter.get("role =", name)).size();
			result.put(name, count);
		});
		return result;
	}

	@ModelMethod(functionality = Functionality.GET_ROLE_REALMS)
	public static Map<Integer, String> listRealms() {
		Map<Integer, String> result = new FluentHashMap<>();
		for (RoleRealm realm : RoleRealm.values()) {
			result.put(realm.getValue(), Utils.prettify(realm.toString()));
		}
		return result;
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static Map<Integer, String> getRealmFunctionalities(RoleRealm realm) {

		Map<Integer, String> result = new FluentHashMap<>();
		// List<Integer> f = getRoleFunctionalities(getDefaultRole(realm));
		List<Integer> f = realm.spec();

		f.forEach(k -> {
			Functionality o = Functionality.from(k);
			if (o.getIsVisible()) {
				result.put(k, o.getName());
			}
		});
		return result;
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static List<Integer> getRoleFunctionalities(String name) {
		UserRoleEntity entity = ofy().load().type(UserRoleEntity.class).id(name).safe();
		return entity.getSpec();
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static void updateRoleSpec(String name, RoleUpdateAction action, Integer functionality) {

		UserRoleEntity entity = ofy().load().type(UserRoleEntity.class).id(name).safe();
		List<Integer> functions = entity.getSpec();

		switch (action) {
		case ADD:
			functions.add(functionality);
			break;
		case REMOVE:
			functions.remove(functionality);
			break;
		}
		entity.setSpec(functions);
		ofy().save().entity(entity).now();
	}

	@ModelMethod(functionality = Functionality.GET_ROLE_REALMS)
	public static RoleRealm getRealm(String name) {
		UserRoleEntity entity = ofy().load().type(UserRoleEntity.class).id(name).safe();
		return RoleRealm.from(entity.getRealm());
	}

	@ModelMethod(functionality = Functionality.MANAGE_ROLES)
	public static String getDefaultRole(RoleRealm realm) {
		return ofy().load().type(UserRoleEntity.class).filter("realm =", realm.getValue()).filter("isDefault", true)
				.first().safe().getName();
	}

	private static boolean isRoleInUse(String name) {
		return !EntityUtils.query(BaseUserEntity.class, QueryFilter.get("role =", name)).isEmpty();
	}

	private static boolean isDefaultRole(String name) {
		UserRoleEntity entity = ofy().load().type(UserRoleEntity.class).id(name).safe();
		return entity.getIsDefault();
	}

	protected static boolean isRoleValid(String name) {
		try {
			return getRealm(name) != null;
		} catch (NotFoundException e) {
			return false;
		}
	}

	public static boolean isAccessAllowed(String roleName, Functionality... functionalities) {

		List<Integer> Userfunctionalities = getRoleFunctionalities(roleName);

		for (Functionality f : functionalities) {
			if (!Userfunctionalities.contains(f.getId())) {
				return false;
			}
		}

		return true;
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}

}
