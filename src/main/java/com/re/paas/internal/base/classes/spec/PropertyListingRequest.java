package com.re.paas.internal.base.classes.spec;

import com.re.paas.internal.base.classes.ListingFilter;

public abstract class PropertyListingRequest {

	private PropertyContractType contractType;

	public PropertyContractType getContractType() {
		return contractType;
	}

	public PropertyListingRequest setContractType(PropertyContractType contractType) {
		this.contractType = contractType;
		return this;
	}
	
	protected ListingFilter getDefaultListingFilter(){
		return new ListingFilter("contractType", contractType.getValue());
	}
	
	public abstract ListingFilter getListingFilter();	
}
