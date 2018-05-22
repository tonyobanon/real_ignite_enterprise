package com.re.paas.internal.clustering.events;

import java.util.Collection;

import com.re.paas.internal.clustering.classes.BaseNodeSpec;

public class NodeJoinStartEvent extends NodeJoinEvent {

	private static final long serialVersionUID = 1L;
	
	private Collection<BaseNodeSpec> spec;

	public Collection<BaseNodeSpec> getSpec() {
		return spec;
	}

	public NodeJoinStartEvent setSpec(Collection<BaseNodeSpec> spec) {
		this.spec = spec;
		return this;
	}

	@Override
	public String name() {
		return "NodeJoinStartEvent";
	}
}
