package com.re.paas.internal.clustering.events;

import com.re.paas.internal.events.BaseEvent;

public class NodeLeaveEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;

	private String nodeId;
	
	@Override
	public String name() {
		return "NodeLeaveEvent";
	}

	public String getNodeId() {
		return nodeId;
	}

	public NodeLeaveEvent setNodeId(String nodeId) {
		this.nodeId = nodeId;
		return this;
	}
}
