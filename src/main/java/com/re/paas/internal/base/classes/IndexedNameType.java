package com.re.paas.internal.base.classes;

public enum IndexedNameType {

	USER(0), ACTIVITY_STREAM(1), AGENT_ORGANIZATION_APPLICATION(2), AGENT_APPLICATION(3), ADMIN_APPLICATION(
			4), AGENT_ORGANIZATION(5), AGENT(6), AGENT_ORGANIZATION_MESSAGE(7), AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGE(8),
			AGENT_ORGANIZATION_REVIEW(9), PROPERTY_SPEC(10), PROPERTY_DESCRIPTOR(11), CRON_JOB(12);

	private int value;

	private IndexedNameType(Integer value) {

		this.value = value;
	}

	public static IndexedNameType from(int value) {

		switch (value) {

		case 0:
			return IndexedNameType.USER;
		case 1:
			return IndexedNameType.ACTIVITY_STREAM;
		case 2:
			return IndexedNameType.AGENT_ORGANIZATION_APPLICATION;
		case 3:
			return IndexedNameType.AGENT_APPLICATION;
		case 4:
			return IndexedNameType.ADMIN_APPLICATION;
		case 5:
			return IndexedNameType.AGENT_ORGANIZATION;
		case 6:
			return IndexedNameType.AGENT;
		case 7:
			return IndexedNameType.AGENT_ORGANIZATION_MESSAGE;
		case 8:
			return IndexedNameType.AGENT_ORGANIZATION_WHISTLEBLOW_MESSAGE;
		case 9:
			return IndexedNameType.AGENT_ORGANIZATION_REVIEW;
		case 10:
			return IndexedNameType.PROPERTY_SPEC;
		case 11:
			return IndexedNameType.PROPERTY_DESCRIPTOR;
		case 12:
			return IndexedNameType.CRON_JOB;
		default:
			throw new IllegalArgumentException("An invalid value was provided");
		}
	}

	public Integer getValue() { 
		return value;
	}
}
