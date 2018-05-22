package com.re.paas.internal.base.api.event_streams;

import java.util.ArrayList;
import java.util.List;

import com.re.paas.internal.core.users.Functionality;

public class SubjectEntity {

	private SubjectType kind;
	private List<Object> identifiers = new ArrayList<>();

	public static SubjectEntity get(SubjectType kind) {
		return new SubjectEntity(kind);
	}
	
	private SubjectEntity(SubjectType kind) {
		this.kind = kind;
	}

	public Functionality getFunctionality() {
		return kind.getFunctionality();
	}

	public List<Object> getIdentifiers() {
		return identifiers;
	}
	
	public SubjectEntity addIdentifier(Object identifier) {
		this.identifiers.add(identifier);
		return this;
	}
	
	public SubjectEntity setIdentifiers(List<Object> identifiers) {
		this.identifiers = identifiers;
		return this;
	}	
	
	@Override
	public String toString() {
		return Utils.stringify(this);
	}
}
