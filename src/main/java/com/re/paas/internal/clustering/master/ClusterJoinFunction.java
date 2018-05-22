package com.re.paas.internal.clustering.master;

import java.util.List;

import com.google.common.collect.Lists;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.clustering.ClusterFunction;
import com.re.paas.internal.clustering.ClusterNode;
import com.re.paas.internal.clustering.Role;
import com.re.paas.internal.clustering.classes.BaseNodeSpec;
import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.classes.NodeJoinRequest;
import com.re.paas.internal.clustering.classes.NodeJoinResult;
import com.re.paas.internal.clustering.classes.NodeState;
import com.re.paas.internal.clustering.events.NodeJoinEvent;
import com.re.paas.internal.clustering.events.NodeStateChangeEvent;
import com.re.paas.internal.clustering.functions.BaseFunction;
import com.re.paas.internal.clustering.nic_utils.IPAddresses;
import com.re.paas.internal.clustering.odyssey.ClientBuilder;
import com.re.paas.internal.events.BaseEvent;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.utils.Utils;

public class ClusterJoinFunction extends ClusterFunction<NodeJoinRequest, NodeJoinResult> {

	@Override
	public Role[] roles() {
		return new Role[] { Role.MASTER };
	}

	@Override
	public BaseFunction id() {
		return BaseFunction.CLUSTER_JOIN;
	}

	@Override
	public NodeJoinResult delegate(NodeJoinRequest t) {

		// validate credentials
		Logger.get().info("Received NodeJoinRequest from " + t.getRemoteAddress());

		ClusterCredentials credentials = CloudEnvironment.get().credentials();

		if (!(t.getCredentials().getAccessKey().equals(credentials.getAccessKey())
				&& t.getCredentials().getSecretKey().equals(credentials.getSecretKey()))) {

			Logger.get().info("Authentication failed for node: " + t.getRemoteAddress());
			return new NodeJoinResult().setSuccess(false).setMessage("Authentication failed");
		}

		Logger.get().info("Authentication succeeded for node: " + t.getRemoteAddress());

		String nodeId = Utils.newShortRandom();

		while (ClusterNode.nodes.containsKey(nodeId)) {
			nodeId = Utils.newShortRandom();
		}

		final BaseNodeSpec nodeSpec = new BaseNodeSpec().setId(nodeId).setName(t.getNodeName())
				.setRemoteAddress(t.getRemoteAddress().toString()).setInboundPort(t.getInboundPort())
				.setState(NodeState.STARTING).setRole(t.getJoinWorkflow().getWorkflow().getRole())
				.setJoinDate(Dates.now());

		// Dispatch NodeJoinEvent to nodeSpec.getRemoteAddress(), for it to register
		// current cluster nodes
		new ClientBuilder<BaseEvent, Object>().build(IPAddresses.getAddress(nodeSpec.getRemoteAddress()),
				nodeSpec.getInboundPort(), BaseFunction.ASYNC_DISPATCH_EVENT,
				new NodeJoinEvent().setSpec(Lists.newArrayList(ClusterNode.nodes.values())));

		List<BaseNodeSpec> nodes = Lists.newArrayList(ClusterNode.nodes.values());
		nodes.add(nodeSpec);

		
		
		
		// Dispatch NodeJoinEvent event to all current cluster nodes, for them to
		// register nodeSpec
		nodes.forEach((v) -> {
			new ClientBuilder<BaseEvent, Object>().build(IPAddresses.getAddress(v.getRemoteAddress()),
					v.getInboundPort(), BaseFunction.DISPATCH_EVENT,
					new NodeJoinEvent().setSpec(FluentArrayList.asList(nodeSpec)));
		});

		// Dispatch NodeStateChangeEvent event to all current cluster nodes, for them to update
		// the status of nodeSpec
		nodes.forEach((v) -> {
			new ClientBuilder<BaseEvent, Object>().build(IPAddresses.getAddress(v.getRemoteAddress()),
					v.getInboundPort(), BaseFunction.ASYNC_DISPATCH_EVENT, new NodeStateChangeEvent()
							.setNodeId(nodeSpec.getId()).setNewState(NodeState.ONLINE));
		});

		NodeJoinResult result = new NodeJoinResult().setSuccess(true).setNodeId(nodeId)
				.setMasterNodeId(ClusterNode.getClusterUniqueId());
		return result;
	}

}
