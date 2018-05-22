package com.re.paas.internal.base.api.event_streams;

import com.re.paas.internal.core.users.Functionality;

public enum SubjectType {

	USER(Functionality.GET_USER_PROFILE);

	private final Functionality functionality;
	
	private SubjectType(Functionality functionality) {
		this.functionality = functionality;
	}

	public Functionality getFunctionality() {
		return functionality;
	}
}
