package com.re.paas.internal.clustering.datagrid;

import com.re.paas.internal.events.BaseEvent;

public class ReplicationCompleteEvent extends BaseEvent{
	
	private static final long serialVersionUID = 1L;
	private final String oldNode;
	private final String newNode;
	

	public ReplicationCompleteEvent(String oldNode, String newNode) {
		super();
		this.oldNode = oldNode;
		this.newNode = newNode;
	}

	public String getOldNode() {
		return oldNode;
	}

	public String getNewNode() {
		return newNode;
	}
	
	@Override
	public String name() {
		return "ReplicationCompleteEvent";
	}
	
}
