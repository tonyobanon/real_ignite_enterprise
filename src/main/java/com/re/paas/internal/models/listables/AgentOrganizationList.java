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
import com.re.paas.internal.base.classes.spec.BaseAgentOrganizationSpec;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.directory.AgentOrganizationEntity;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.LocationModel;
import com.re.paas.internal.models.RoleModel;

public class AgentOrganizationList extends Listable<BaseAgentOrganizationSpec> {

	@Override
	public IndexedNameType type() {
		return IndexedNameType.AGENT_ORGANIZATION;
	}

	@Override
	public boolean authenticate(ListingType type, Long userId, List<ListingFilter> listingFilters) {
		String role = BaseUserModel.getRole(userId);
		switch (type) {
		case SEARCH:
			return RoleModel.isAccessAllowed(role, Functionality.SEARCH_AGENT_ORGANIZATION);
		case LIST:
		default:
			return RoleModel.isAccessAllowed(role, Functionality.LIST_AGENT_ORGANIZATION);
		}
	}

	@Override
	public Class<AgentOrganizationEntity> entityType() {
		return AgentOrganizationEntity.class;
	}

	@Override
	public boolean searchable() {
		return true;
	}

	@Override
	public Map<Long, BaseAgentOrganizationSpec> getAll(List<String> keys) {

		Map<Long, BaseAgentOrganizationSpec> result = new FluentHashMap<>();

		List<Long> longKeys = new ArrayList<>(keys.size());

		// Convert to Long keys
		keys.forEach(k -> {
			longKeys.add(Long.parseLong(k));
		});

		ofy().load().type(AgentOrganizationEntity.class).ids(longKeys).forEach((k, v) -> {

			BaseAgentOrganizationSpec spec = new BaseAgentOrganizationSpec().setId(v.getId()).setName(v.getName())
					.setEmail(v.getEmail()).setLogo(v.getLogo()).setRating(v.getRating()).setAddress(v.getAddress())
					.setCity(v.getCity()).setCityName(LocationModel.getCityName(v.getCity().toString()))
					.setTerritory(v.getTerritory()).setTerritoryName(LocationModel.getTerritoryName(v.getTerritory()))
					.setCountry(v.getCountry()).setCountryName(LocationModel.getCountryName(v.getCountry()));

			result.put(k, spec);
		});

		return result;
	}

	@Override
	public SearchableUISpec searchableUiSpec() {
		return new SearchableUISpec().setName("agent_organization").setListingPageUrl("/agent-organization-search");
	}

}
