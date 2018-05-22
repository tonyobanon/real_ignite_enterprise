package com.re.paas.internal.models.listables;

import java.util.List;

import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.models.BaseAgentModel;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

public abstract class AbstractAgentOrganizationList<T> extends Listable<T> {

	static boolean canProvisionOrganization(Long userId, List<ListingFilter> listingFilters) {

		String role = BaseUserModel.getRole(userId);
		RoleRealm realm = RoleModel.getRealm(role);

		if (realm.equals(RoleRealm.ADMIN)) {
			return true;
		}

		boolean canProvisionOrganization = false;

		for (ListingFilter lf : listingFilters) {

			Object agentOrganizationObj = lf.getFilters().get("agentOrganization");
			if (agentOrganizationObj == null) {
				continue;
			}

			Long agentOrganization = Long.parseLong(agentOrganizationObj.toString());
			Long principalAgentOrganization = BaseAgentModel.getAgentOrganization(realm, userId);

			canProvisionOrganization =
					// Admin
					principalAgentOrganization == null || agentOrganization.equals(principalAgentOrganization);
		}

		return canProvisionOrganization;
	}

}
