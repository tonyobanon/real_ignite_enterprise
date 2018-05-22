package com.re.paas.internal.clustering.classes;

import java.io.Serializable;

public class NodeJoinResult implements Serializable {

	private static final long serialVersionUID = 1L;

	private boolean isSuccess;
	private String message;

	private String nodeId;
	private String masterNodeId;

	public boolean isSuccess() {
		return isSuccess;
	}

	public NodeJoinResult setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
		return this;
	}

	public String getMessage() {
		return message;
	}

	public NodeJoinResult setMessage(String message) {
		this.message = message;
		return this;
	}

	public String getNodeId() {
		return nodeId;
	}

	public NodeJoinResult setNodeId(String nodeId) {
		this.nodeId = nodeId;
		return this;
	}

	public String getMasterNodeId() {
		return masterNodeId;
	}

	public NodeJoinResult setMasterNodeId(String masterNodeId) {
		this.masterNodeId = masterNodeId;
		return this;
	}
}
