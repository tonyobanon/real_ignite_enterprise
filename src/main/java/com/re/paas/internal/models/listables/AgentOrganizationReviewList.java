package com.re.paas.internal.models.listables;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.ListingType;
import com.re.paas.internal.base.classes.SearchableUISpec;
import com.re.paas.internal.base.classes.spec.AgentOrganizationReviewSpec;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.directory.AgentOrganizationReviewEntity;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;
import com.re.paas.internal.models.helpers.EntityHelper;

public class AgentOrganizationReviewList extends Listable<AgentOrganizationReviewSpec> {

	@Override
	public IndexedNameType type() {
		return IndexedNameType.AGENT_ORGANIZATION_REVIEW;
	}

	@Override
	public boolean authenticate(ListingType type, Long principal, List<ListingFilter> listingFilters) {

		String role = BaseUserModel.getRole(principal);
		return RoleModel.isAccessAllowed(role, Functionality.VIEW_AGENT_ORGANIZATION_REVIEWS);
	}

	@Override
	public Class<AgentOrganizationReviewEntity> entityType() {
		return AgentOrganizationReviewEntity.class;
	}

	@Override
	public boolean searchable() {
		return false;
	}

	@Override
	public Map<Long, AgentOrganizationReviewSpec> getAll(List<String> keys) {

		Map<Long, AgentOrganizationReviewSpec> result = new FluentHashMap<>();

		List<Long> longKeys = new ArrayList<>(keys.size());

		// Convert to Long keys
		keys.forEach(k -> {
			longKeys.add(Long.parseLong(k));
		});

		ofy().load().type(AgentOrganizationReviewEntity.class).ids(longKeys).forEach((k, v) -> {
			
			AgentOrganizationReviewSpec spec = EntityHelper.toObjectModel(v);
			
			result.put(k, spec);
		});

		return result;
	}

	@Override
	public SearchableUISpec searchableUiSpec() {
		return null;
	}

}
