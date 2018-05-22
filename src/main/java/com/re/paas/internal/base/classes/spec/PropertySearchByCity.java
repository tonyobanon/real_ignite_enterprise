package com.re.paas.internal.base.classes.spec;

import com.re.paas.internal.base.classes.ListingFilter;

public class PropertySearchByCity extends PropertyListingRequest {

	private Integer cityId;

	public Integer getCityId() {
		return cityId;
	}

	public PropertySearchByCity setCityId(Integer cityId) {
		this.cityId = cityId;
		return this;
	}
	
	@Override
	public ListingFilter getListingFilter() {
		
		ListingFilter filter = getDefaultListingFilter()
				.addFilter("cityId", cityId);
	
		return filter;
	}

}
