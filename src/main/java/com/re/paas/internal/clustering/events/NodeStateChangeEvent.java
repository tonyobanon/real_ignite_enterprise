package com.re.paas.internal.clustering.events;

import com.re.paas.internal.clustering.classes.NodeState;
import com.re.paas.internal.events.BaseEvent;

public class NodeStateChangeEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;

	private String nodeId;
	private NodeState newState;
	
	@Override
	public String name() {
		return "NodeStateChangeEvent";
	}

	public String getNodeId() {
		return nodeId;
	}

	public NodeStateChangeEvent setNodeId(String nodeId) {
		this.nodeId = nodeId;
		return this;
	}

	public NodeState getNewState() {
		return newState;
	}

	public NodeStateChangeEvent setNewState(NodeState newState) {
		this.newState = newState;
		return this;
	}
}
