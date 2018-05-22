package com.re.paas.internal.base.classes.spec;

import com.re.paas.internal.base.classes.ListingFilter;

public class PropertySearchByAgentOrganization extends PropertyListingRequest {

	private Long agentOrganization;

	public Long getAgentOrganization() {
		return agentOrganization;
	}

	public PropertySearchByAgentOrganization setAgentOrganization(Long agentOrganization) {
		this.agentOrganization = agentOrganization;
		return this;
	}

	@Override
	public ListingFilter getListingFilter() {
		
		ListingFilter filter = getDefaultListingFilter()
				.addFilter("agentOrganization", agentOrganization);
	
		return filter;
	}

}
