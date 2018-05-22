package com.re.paas.internal.models.listables;

import static com.googlecode.objectify.ObjectifyService.ofy;

import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.CronInterval;
import com.re.paas.internal.base.classes.CronType;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.base.classes.IndexedNameType;
import com.re.paas.internal.base.classes.ListingFilter;
import com.re.paas.internal.base.classes.ListingType;
import com.re.paas.internal.base.classes.spec.BaseCronJobSpec;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.CronJobEntity;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;

public class CronJobList extends Listable<BaseCronJobSpec>{

	@Override
	public IndexedNameType type() {
		return IndexedNameType.CRON_JOB;
	}

	@Override
	public boolean authenticate(ListingType type, Long userId, List<ListingFilter> listingFilters) {
		return RoleModel.isAccessAllowed(BaseUserModel.getRole(userId), Functionality.VIEW_CRON_JOBS);
	}

	@Override
	public Class<CronJobEntity> entityType() {
		return CronJobEntity.class;
	}
	
	@Override
	public boolean searchable() {
		return false;
	}

	@Override
	public Map<String, BaseCronJobSpec> getAll(List<String> keys) {
		
		Map<String, BaseCronJobSpec> result = new FluentHashMap<>();
		
		keys.forEach(k -> {
			
			Long id = Long.parseLong(k);
			
			CronJobEntity e = ofy().load().type(CronJobEntity.class).id(id).safe();
			
			BaseCronJobSpec spec = new BaseCronJobSpec()
					.setId(e.getId())
					.setName(e.getName())
					.setInterval(CronInterval.from(e.getInterval()))
					.setCronType(CronType.from(e.getCronType()))
					.setIsReady(e.getIsReady())
					.setTotalExecutionCount(e.getTotalExecutionCount())
					.setMaxExecutionCount(e.getMaxExecutionCount())
					.setIsInternal(e.getIsInternal())
					.setDateCreated(e.getDateCreated());
			
			result.put(k, spec);
		});
		
		return result;
	}

}
