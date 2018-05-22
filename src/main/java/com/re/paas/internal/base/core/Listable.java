package com.re.paas.internal.base.core;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.ListingType;
import com.re.paas.internal.base.classes.SearchableUISpec;

public abstract class Listable<S> {

	public abstract IndexedNameType type();

	/**
	 * This retrieves a set of objects. It is advised to use a batch get operation
	 * where possible, to reduce costs.
	 */
	public abstract Map<?, S> getAll(List<String> keys);

	/**
	 * This authenticates the user that wants to access this data table
	 */
	public abstract boolean authenticate(ListingType type, Long userId, List<ListingFilter> listingFilters);

	public abstract Class<?> entityType();

	public abstract boolean searchable();

	public SearchableUISpec searchableUiSpec() {
		return null;
	}
	
	public List<ListingFilter> defaultListingFilters() {
		return Lists.newArrayList();
	}
	
	public boolean canCreateContext() {
		return true;
	}

}
