package com.re.paas.internal.base.api.event_streams;

import com.re.paas.internal.base.classes.ClientAware;
import com.re.paas.internal.base.classes.ClientRBRef;

public enum CoordinatingConjuction {
	
	FOR, AND, NOR, BUT, OR, YET, SO;
	
	@Override
	@ClientAware
	public String toString() {
		return ClientRBRef.get(this.name().toLowerCase()).toString();
	}
}
