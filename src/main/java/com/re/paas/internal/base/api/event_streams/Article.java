package com.re.paas.internal.base.api.event_streams;

import com.re.paas.internal.base.classes.ClientAware;
import com.re.paas.internal.base.classes.ClientRBRef;

public enum Article {

	A(false), AN(false), THE(true), HIS(false), HER(false);
	
	private final boolean isDefinite;
	
	private Article(boolean isDefinite) {
		this.isDefinite = isDefinite;
	}

	public boolean isDefinite() {
		return isDefinite;
	}
	
	@Override
	@ClientAware
	public String toString() {
		return ClientRBRef.get(this.name().toLowerCase()).toString();
	}
	
}
