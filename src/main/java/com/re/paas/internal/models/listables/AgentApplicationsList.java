package com.re.paas.internal.models.listables;

import java.util.List;

import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.ListingType;
import com.re.paas.internal.base.classes.SearchableUISpec;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.core.users.RoleRealm;
import com.re.paas.internal.entites.ApplicationEntity;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

public class AgentApplicationsList extends AbstractApplicationsList {

	private static final List<ListingFilter> DEFAULT_LISTING_FILTERS = new FluentArrayList<ListingFilter>()
			.with(new ListingFilter("role", RoleModel.getDefaultRole(RoleRealm.AGENT)));

	@Override
	public IndexedNameType type() {
		return IndexedNameType.AGENT_APPLICATION;
	}

	@Override
	public boolean authenticate(ListingType type, Long userId, List<ListingFilter> listingFilters) {
		return 
				RoleModel.isAccessAllowed(BaseUserModel.getRole(userId), Functionality.VIEW_AGENT_APPLICATIONS)
				&&
				AbstractAgentOrganizationList.canProvisionOrganization(userId, listingFilters);
	}

	@Override
	public Class<ApplicationEntity> entityType() {
		return ApplicationEntity.class;
	}

	@Override
	public boolean searchable() {
		return true;
	}

	@Override
	public SearchableUISpec searchableUiSpec() {
		return new SearchableUISpec().setName("agent_applications").setListingPageUrl("/agent-application-search");
	}

	@Override
	public List<ListingFilter> defaultListingFilters() {
		return DEFAULT_LISTING_FILTERS;
	}
}
