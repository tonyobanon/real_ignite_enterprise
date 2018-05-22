package com.re.paas.internal.models.listables;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.ApplicationStatus;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.IndexedNameSpec;
import com.re.paas.internal.base.classes.spec.BaseApplicationSpec;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.entites.ApplicationEntity;
import com.re.paas.internal.models.ApplicationModel;
import com.re.paas.internal.models.RoleModel;

public abstract class AbstractApplicationsList extends Listable<BaseApplicationSpec> {

	@Override
	public Map<String, BaseApplicationSpec> getAll(List<String> keys) {

		Map<String, BaseApplicationSpec> result = new FluentHashMap<>();

		keys.forEach(k -> {

			Long applicationId = Long.parseLong(k);

			String role = ApplicationModel.getApplicationRole(applicationId);
			RoleRealm realm = RoleModel.getRealm(role);

			IndexedNameSpec nameSpec = ApplicationModel.getNameSpec(applicationId, realm);

			ApplicationEntity e = ofy().load().type(ApplicationEntity.class).id(applicationId).safe();

			BaseApplicationSpec spec = new BaseApplicationSpec().setId(applicationId).setRole(e.getRole())
					.setStatus(ApplicationStatus.from(e.getStatus())).setNameSpec(nameSpec)
					.setDateCreated(e.getDateCreated()).setDateUpdated(e.getDateUpdated());

			result.put(k, spec);

		});

		return result;
	}

}
