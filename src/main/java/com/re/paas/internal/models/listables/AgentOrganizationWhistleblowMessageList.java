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
import com.re.paas.internal.base.classes.spec.BaseAgentOrganizationWhistleblowMessageSpec;
import com.re.paas.internal.base.classes.spec.IssueResolution;
import com.re.paas.internal.base.core.Listable;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.entites.directory.AgentOrganizationWhistleblowMessageEntity;
import com.re.paas.internal.models.BaseAgentModel;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;
import com.re.paas.internal.utils.Utils;

public class AgentOrganizationWhistleblowMessageList extends Listable<BaseAgentOrganizationWhistleblowMessageSpec> {

	@Override
	public IndexedNameType type() {
		return IndexedNameType.AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGE;
	}

	@Override
	public boolean authenticate(ListingType type, Long principal, List<ListingFilter> listingFilters) {

		String role = BaseUserModel.getRole(principal);
		boolean canAccess = RoleModel.isAccessAllowed(role, Functionality.LIST_AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGES);
		
		return canAccess;
	}

	@Override
	public Class<AgentOrganizationWhistleblowMessageEntity> entityType() {
		return AgentOrganizationWhistleblowMessageEntity.class;
	}

	@Override
	public boolean searchable() {
		return false;
	}

	@Override
	public Map<Long, BaseAgentOrganizationWhistleblowMessageSpec> getAll(List<String> keys) {

		Map<Long, BaseAgentOrganizationWhistleblowMessageSpec> result = new FluentHashMap<>();

		List<Long> longKeys = new ArrayList<>(keys.size());

		// Convert to Long keys
		keys.forEach(k -> {
			longKeys.add(Long.parseLong(k));
		});

		ofy().load().type(AgentOrganizationWhistleblowMessageEntity.class).ids(longKeys).forEach((k, v) -> {
			
			BaseAgentOrganizationWhistleblowMessageSpec spec = new BaseAgentOrganizationWhistleblowMessageSpec()
					.setId(v.getId())
					.setName(v.getName())
					.setAgentOrganization(v.getAgentOrganization())
					.setAgentOrganizationName(BaseAgentModel.getAgentOrganizationName(v.getAgentOrganization()))
					.setMobile(v.getMobile())
					.setEmail(v.getEmail())
					.setIsRead(v.getIsRead())
					.setResolution(IssueResolution.from(v.getResolution()))
					.setTruncatedMessage(Utils.truncate(v.getMessage(), 10))
					.setDateCreated(v.getDateCreated())
					.setDateUpdated(v.getDateUpdated());
			
			
			result.put(k, spec);
		});

		return result;
	}

	@Override
	public SearchableUISpec searchableUiSpec() {
		return null;
	}

}
