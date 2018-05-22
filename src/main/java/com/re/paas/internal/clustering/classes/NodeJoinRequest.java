package com.re.paas.internal.clustering.classes;

import java.util.Map;

import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.cloud_provider.Tags;
import com.re.paas.internal.clustering.ClusterNode;
import com.re.paas.internal.clustering.NodeRequest;

public class NodeJoinRequest extends NodeRequest {

	private static final long serialVersionUID = 1L;

	private String clusterName;
	private Integer inboundPort;

	private String nodeName;

	private ClusterCredentials credentials;
	private ClusterJoinWorkflow joinWorkflow;

	public String getClusterName() {
		return clusterName;
	}

	public NodeJoinRequest setClusterName(String clusterName) {
		this.clusterName = clusterName;
		return this;
	}

	public Integer getInboundPort() {
		return inboundPort;
	}

	public NodeJoinRequest setInboundPort(Integer inboundPort) {
		this.inboundPort = inboundPort;
		return this;
	}

	public String getNodeName() {
		return nodeName;
	}

	public NodeJoinRequest setNodeName(String nodeName) {
		this.nodeName = nodeName;
		return this;
	}

	public ClusterCredentials getCredentials() {
		return credentials;
	}

	public NodeJoinRequest setCredentials(ClusterCredentials credentials) {
		this.credentials = credentials;
		return this;
	}

	public ClusterJoinWorkflow getJoinWorkflow() {
		return joinWorkflow;
	}

	public NodeJoinRequest setJoinWorkflow(ClusterJoinWorkflow joinWorkflow) {
		this.joinWorkflow = joinWorkflow;
		return this;
	}

	public static NodeJoinRequest get() {

		CloudEnvironment env = CloudEnvironment.get();
		Map<String, String> tags = env.getInstanceTags();
		
		ClusterJoinWorkflow workflow = env.joinWorkflow();
		
		String name = tags.get(Tags.NODE_NAME_TAG) != null ? tags.get(Tags.NODE_NAME_TAG)
				: tags.get(Tags.RESOURCE_NAME_TAG) != null ? tags.get(Tags.RESOURCE_NAME_TAG)
						: ClusterNode.getResourceName(workflow);
		
		return new NodeJoinRequest()
				.setClusterName(ClusterNode.clusterName())
				.setInboundPort(ClusterNode.getInboundPort())
				.setNodeName(name)
				.setCredentials(env.credentials())
				.setJoinWorkflow(workflow);
	}

}
