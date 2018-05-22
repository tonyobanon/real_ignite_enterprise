package com.re.paas.internal.clustering.events;

import java.util.Collection;

import com.re.paas.internal.clustering.classes.BaseNodeSpec;
import com.re.paas.internal.events.BaseEvent;

public class NodeJoinEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;
	
	private Collection<BaseNodeSpec> spec;

	public Collection<BaseNodeSpec> getSpec() {
		return spec;
	}

	public NodeJoinEvent setSpec(Collection<BaseNodeSpec> spec) {
		this.spec = spec;
		return this;
	}

	@Override
	public String name() {
		return "NodeJoinEvent";
	}
}
