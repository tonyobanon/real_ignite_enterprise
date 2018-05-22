package com.re.paas.internal.core.users;

import java.util.List;

import com.re.paas.internal.base.classes.FluentArrayList;

public enum RoleRealm {

	ADMIN(0, 20, true), ORGANIZATION_ADMIN(1, 15, true), AGENT(2, 10, true), CUSTOMER(3, 5, false);

	private final Integer value;
	private final Integer authority;
	private final boolean requiresApplicationReview;

	private RoleRealm(Integer value, Integer authority, boolean requiresApplicationReview) {
		this.value = value;
		this.authority = authority;
		this.requiresApplicationReview = requiresApplicationReview;
	}

	public static RoleRealm from(int value) {

		switch (value) {

		case 0:
			return RoleRealm.ADMIN;

		case 1:
			return RoleRealm.ORGANIZATION_ADMIN;

		case 2:
			return RoleRealm.AGENT;

		case 3:
			return RoleRealm.CUSTOMER;

		default:
			throw new IllegalArgumentException("An invalid value was provided");
		}
	}

	public Integer getValue() {
		return value;
	}

	public Integer getAuthority() {
		return authority;
	}

	public boolean isRequiresApplicationReview() {
		return requiresApplicationReview;
	}

	public List<Integer> spec() {
		List<Integer> result = null;
		switch (this) {
		case ADMIN:
			result = adminFunctionalities();
			break;
		case ORGANIZATION_ADMIN:
			result = organizationAdminFunctionalities();
			break;
		case AGENT:
			result = agentFunctionalities();
			break;
		case CUSTOMER:
			result = customerFunctionalities();
			break;
		}
		return result;
	}

	public static List<Integer> adminFunctionalities() {
		List<Integer> o = new FluentArrayList<Integer>();
		for (Functionality f : Functionality.values()) {
			if (f.getId() > 0) {
				o.add(f.getId());
			}
		}
		return o;
	}

	private static List<Integer> baseUserFunctionalities() {
		return new FluentArrayList<Integer>().with(Functionality.VIEW_OWN_PROFILE.getId())
				.with(Functionality.MANAGE_OWN_PROFILE.getId());
	}

	private static List<Integer> customerFunctionalities() {
		return new FluentArrayList<Integer>().withAll(baseUserFunctionalities())
				.with(Functionality.ADD_TO_OWN_SAVED_LIST.getId())
				.with(Functionality.REMOVE_FROM_OWN_SAVED_LIST.getId()).with(Functionality.GET_OWN_SAVED_LIST.getId());
	}

	private static List<Integer> basePrincipalFunctionalities() {
		return new FluentArrayList<Integer>()

				.withAll(baseUserFunctionalities())

				.with(Functionality.GET_SEARCHABLE_LISTS.getId()).with(Functionality.GET_PERSON_NAMES.getId())
				.with(Functionality.GET_REALM_FUNCTIONALITIES.getId())
				.with(Functionality.GET_ROLE_FUNCTIONALITIES.getId())

				.with(Functionality.GET_USER_PROFILE.getId());
	}

	private static List<Integer> agentFunctionalities() {
		return new FluentArrayList<Integer>()

				.withAll(basePrincipalFunctionalities())

				.with(Functionality.LIST_AGENT_ORGANIZATION_MESSAGES.getId())
				.with(Functionality.UPDATE_AGENT_ORGANIZATION_MESSAGES.getId())
				.with(Functionality.DELETE_AGENT_ORGANIZATION_MESSAGES.getId())
				.with(Functionality.VIEW_AGENT_ORGANIZATION_MESSAGE.getId())

				.with(Functionality.CREATE_PROPERTY_CREATION_REQUEST.getId())
				.with(Functionality.CREATE_PROPERTY_UPDATE_REQUEST.getId())
				.with(Functionality.CREATE_PROPERTY_DELETION_REQUEST.getId())

				.with(Functionality.CREATE_PROPERTY_LISTING.getId()).with(Functionality.UPDATE_PROPERTY_LISTING.getId())
				.with(Functionality.DELETE_PROPERTY_LISTING.getId())

				.with(Functionality.GET_PROPERTY_LISTINGS.getId())
				.with(Functionality.UPDATE_PROPERTY_LISTING_AVAILABILITY_STATUS.getId())

				.with(Functionality.ADD_TO_USER_SAVED_LIST.getId())
				.with(Functionality.REMOVE_FROM_USER_SAVED_LIST.getId()).with(Functionality.GET_USER_SAVED_LIST.getId())

				.with(Functionality.CREATE_PROPERTY_PRICE_RULE.getId())
				.with(Functionality.VIEW_PROPERTY_PRICE_RULES.getId())
				.with(Functionality.UPDATE_PROPERTY_PRICE_RULE.getId())
				.with(Functionality.DELETE_PROPERTY_PRICE_RULE.getId())

				.with(Functionality.UPDATE_AGENT_AVAILABILITY_SCHEDULE.getId())
				.with(Functionality.GET_AGENT_AVAILABILITY_SCHEDULES.getId());
	}

	private static List<Integer> organizationAdminFunctionalities() {
		return new FluentArrayList<Integer>()

				.withAll(agentFunctionalities())

				.with(Functionality.VIEW_AGENT_APPLICATIONS.getId())
				.with(Functionality.REVIEW_AGENT_APPLICATION.getId())

				.with(Functionality.UPDATE_AGENT_ORGANIZATION.getId())

				.with(Functionality.DELETE_AGENT.getId())

				.with(Functionality.UPDATE_AGENT_ORGANIZATION_AVAILABILITY_SCHEDULE.getId())
				.with(Functionality.GET_AGENT_ORGANIZATION_AVAILABILITY_SCHEDULES.getId());
	}

}
