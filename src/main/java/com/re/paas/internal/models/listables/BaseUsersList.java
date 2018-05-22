package com.re.paas.internal.models.listables;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.IndexedNameSpec;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.ListingType;
import com.re.paas.internal.base.classes.SearchableUISpec;
import com.re.paas.internal.base.classes.spec.BaseUserSpec;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.BaseUserEntity;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

public class BaseUsersList extends Listable<BaseUserSpec>{

	@Override
	public IndexedNameType type() {
		return IndexedNameType.USER;
	}

	@Override
	public boolean authenticate(ListingType type, Long userId, List<ListingFilter> listingFilters) {
		return RoleModel.isAccessAllowed(BaseUserModel.getRole(userId), Functionality.GET_USER_PROFILE);
	}

	@Override
	public Class<BaseUserEntity> entityType() {
		return BaseUserEntity.class;
	}
	
	@Override
	public boolean searchable() {
		return true;
	}

	@Override
	public Map<String, BaseUserSpec> getAll(List<String> keys) {
		
		Map<String, BaseUserSpec> result = new FluentHashMap<>();
		
		keys.forEach(k -> {
			Long userId = Long.parseLong(k);
			
			BaseUserEntity e = ofy().load().type(BaseUserEntity.class).id(userId).safe();
			
			IndexedNameSpec nameSpec = new IndexedNameSpec()
						.setKey(userId.toString())
						.setX(e.getFirstName())
						.setY(e.getLastName())
						.setZ(e.getMiddleName());
			
			BaseUserSpec spec = new BaseUserSpec()
					.setId(userId)
					.setRole(e.getRole())
					.setName(nameSpec)
					.setDateCreated(e.getDateCreated())
					.setDateUpdated(e.getDateUpdated());	
			
			result.put(k, spec);
		});
		
		return result;
	}
	
	@Override
	public SearchableUISpec searchableUiSpec() {
		return new SearchableUISpec().setName("users").setListingPageUrl("/users-search");
	}

}
