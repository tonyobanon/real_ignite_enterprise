package com.re.paas.internal.models;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.ClientResources;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.QueryFilter;
import com.re.paas.internal.base.classes.spec.BaseUserSpec;
import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.base.core.ResourceException;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.entites.BaseUserEntity;
import com.re.paas.internal.entites.directory.AgentEntity;
import com.re.paas.internal.entites.directory.AgentOrganizationAdminEntity;
import com.re.paas.internal.entites.directory.AgentOrganizationEntity;
import com.re.paas.internal.models.helpers.EntityUtils;

public class UserModel implements BaseModel {

	@Override
	public String path() {
		return "core/users";
	}

	@BlockerTodo("Reduce <keys> to a definite number. Find fix")
	public static List<BaseUserSpec> getSuggestedProfiles(Long principal, Long userId) {

		if (!BaseUserModel.canAccessUserProfile(principal, userId)) {
			throw new ResourceException(ResourceException.ACCESS_NOT_ALLOWED);
		}

		// Get role
		String role = BaseUserModel.getRole(userId);
		RoleRealm realm = RoleModel.getRealm(role);

		Map<Long, String> keys = new HashMap<>();

		switch (realm) {

		case ADMIN:
			break;

		case ORGANIZATION_ADMIN:

			AgentOrganizationAdminEntity oae = ofy().load().type(AgentOrganizationAdminEntity.class).id(userId).safe();
			AgentOrganizationEntity oe = ofy().load().type(AgentOrganizationEntity.class).id(oae.getAgentOrganization())
					.safe();

			EntityUtils.query(AgentOrganizationEntity.class, QueryFilter.get("city", oe.getCity())).forEach(e -> {
				keys.put(e.getAdmin(), ClientRBRef.forAll("Admin_agent", "at").toString()
						+ ClientResources.HtmlCharacterEntities.SPACE + oe.getName());
			});

			break;

		case AGENT:
			
			AgentEntity ae = ofy().load().type(AgentEntity.class).id(userId).safe();
			AgentOrganizationEntity aoe = ofy().load().type(AgentOrganizationEntity.class).id(ae.getAgentOrganization())
					.safe();

			aoe.getAgents().forEach(e -> {
				keys.put(e, ClientRBRef.forAll("Agent", "at").toString()
						+ ClientResources.HtmlCharacterEntities.SPACE + aoe.getName());
			});
			
			break;
		}

		keys.remove(userId);

		return getBaseUserSpec(keys);
	}

	private static List<BaseUserSpec> getBaseUserSpec(Map<Long, String> keys) {

		List<BaseUserSpec> result = new ArrayList<>();

		ofy().load().type(BaseUserEntity.class).ids(keys.keySet()).forEach((k, v) -> {

			BaseUserSpec spec = new BaseUserSpec().setId(v.getId()).setRole(v.getRole()).setDescription(keys.get(k))
					.setName(v.getFirstName() + ClientResources.HtmlCharacterEntities.SPACE + v.getLastName())
					.setDateCreated(v.getDateCreated()).setDateUpdated(v.getDateUpdated()).setImage(v.getImage());

			result.add(spec);

		});
		return result;
	}

	@Override
	public void install(InstallOptions options) {
		// TODO Auto-generated method stub
		
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
	public void preInstall() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}

}
