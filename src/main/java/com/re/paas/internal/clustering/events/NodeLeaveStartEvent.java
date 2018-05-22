package com.re.paas.internal.clustering.events;

public class NodeLeaveStartEvent extends NodeLeaveEvent {

	private static final long serialVersionUID = 1L;
	
	@Override
	public String name() {
		return "NodeLeaveStartEvent";
	}
}
