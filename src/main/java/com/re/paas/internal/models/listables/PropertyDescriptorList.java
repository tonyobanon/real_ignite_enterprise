package com.re.paas.internal.models.listables;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.ListingType;
import com.re.paas.internal.base.classes.spec.BasePropertyDescriptor;
import com.re.paas.internal.base.classes.spec.PropertyType;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.directory.PropertyEntity;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

public class PropertyDescriptorList extends Listable<BasePropertyDescriptor> {

	@Override
	public IndexedNameType type() {
		return IndexedNameType.PROPERTY_DESCRIPTOR;
	}

	@Override
	public boolean authenticate(ListingType type, Long userId, List<ListingFilter> listingFilters) {
		return RoleModel.isAccessAllowed(BaseUserModel.getRole(userId), Functionality.LIST_PROPERTY);
	}

	@Override
	public Class<PropertyEntity> entityType() {
		return PropertyEntity.class;
	}

	@Override
	public Map<Long, BasePropertyDescriptor> getAll(List<String> keys) {

		Map<Long, BasePropertyDescriptor> result = new FluentHashMap<>();

		List<Long> longKeys = new ArrayList<>(keys.size());

		// Convert to Long keys
		keys.forEach(k -> {
			longKeys.add(Long.parseLong(k));
		});

		ofy().load().type(entityType()).ids(longKeys).forEach((k, v) -> {

			BasePropertyDescriptor spec = new BasePropertyDescriptor()
					.setAddress(v.getAddress())
					.setAgentOrganization(v.getAgentOrganization())
					.setArea(v.getArea())
					.setCity(v.getCity())
					.setCountry(v.getCountry())
					.setDateCreated(v.getDateCreated())
					.setDateUpdated(v.getDateUpdated())
					.setId(v.getId())
					.setPrice(v.getPrice())
					.setCurrency(v.getCurrency())
					.setTerritory(v.getTerritory())
					.setTitle(v.getTitle())
					.setType(PropertyType.from(v.getType()))
					.setZipCode(v.getZipCode());
					
			result.put(k, spec);
		});

		return result;
	}

	@Override
	public boolean searchable() {
		return false;
	}
}
