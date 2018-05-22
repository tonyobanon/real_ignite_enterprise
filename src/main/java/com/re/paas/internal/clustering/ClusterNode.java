package com.re.paas.internal.clustering;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.re.paas.internal.base.AppDelegate;
import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.cloud_provider.Tags;
import com.re.paas.internal.clustering.classes.BaseNodeSpec;
import com.re.paas.internal.clustering.classes.ClusterConfig;
import com.re.paas.internal.clustering.classes.ClusterJoinWorkflow;
import com.re.paas.internal.clustering.classes.JoinWorkflow;
import com.re.paas.internal.clustering.classes.NodeJoinRequest;
import com.re.paas.internal.clustering.classes.NodeJoinResult;
import com.re.paas.internal.clustering.classes.Utils;
import com.re.paas.internal.clustering.events.NodeLeaveEvent;
import com.re.paas.internal.clustering.events.NodeLeaveStartEvent;
import com.re.paas.internal.clustering.functions.BaseFunction;
import com.re.paas.internal.clustering.master.MasterNode;
import com.re.paas.internal.clustering.nic_utils.IPAddresses;
import com.re.paas.internal.clustering.odyssey.ClientBuilder;
import com.re.paas.internal.clustering.odyssey.Sockets;
import com.re.paas.internal.core.cron.Scheduler;
import com.re.paas.internal.errors.ClusteringError;
import com.re.paas.internal.events.BaseEvent;

import io.netty.channel.Channel;

public class ClusterNode {

	public static String clusterUniqueId;

	private static String name;

	private static String clusterName;

	private static Integer inboundPort;

	private static Boolean autoScalingEnabled;

	private static String cloudUniqueId;

	private static InetAddress clusteringAddress;

	private static InetAddress wka;

	protected static Channel channelServer;

	public static Role role;

	public static void setClusterUniqueId(String clusterUniqueId) {
		ClusterNode.clusterUniqueId = clusterUniqueId;
	}

	public static String getClusterUniqueId() {
		return clusterUniqueId;
	}

	public static String clusterName() {
		return clusterName;
	}

	public static Integer getInboundPort() {
		return inboundPort;
	}

	public static Boolean isAutoScalingEnabled() {
		return autoScalingEnabled;
	}

	public static String getCloudUniqueId() {
		return cloudUniqueId;
	}

	public static String getName() {
		return name;
	}

	public static InetAddress getClusteringAddress() {
		return clusteringAddress;
	}

	public static InetAddress getWKA() {
		return wka;
	}

	public static Channel getChannelServer() {
		return channelServer;
	}

	public static Role getRole() {
		return role;
	}

	public static void start() {

		// Set Cluster Name

		clusterName = ClusterConfig.get(ClusterConfig.CLUSTER_NAME);

		if (!ClusterConfig.CLUSTER_NAME_PATTERN.matcher(clusterName).matches()) {
			Exceptions.throwRuntime(
					PlatformException.get(ClusteringError.CLUSTER_NAME_HAS_INCORRECT_FORMAT, clusterName));
		}

		Logger.get().info("Starting node on cluster: " + clusterName);

		// Set node name

		CloudEnvironment env = CloudEnvironment.get();
		Map<String, String> tags = env.getInstanceTags();

		String name = tags.get(Tags.NODE_NAME_TAG) != null ? tags.get(Tags.NODE_NAME_TAG)
				: tags.get(Tags.RESOURCE_NAME_TAG) != null ? tags.get(Tags.RESOURCE_NAME_TAG)
						: ClusterNode.getResourceName(env.joinWorkflow());
		ClusterNode.name = name;

		// Get clustering address

		Logger.get().info("Acquiring clustering address");
		clusteringAddress = CloudEnvironment.get().clusteringHost();

		Logger.get().info("Using clustering address: " + CloudEnvironment.get().clusteringHost().getHostAddress());

		// Set Inbound port

		Integer inboundPort = ClusterConfig.getInteger(ClusterConfig.CLUSTERING_PORT);

		if (!Utils.isPortAvailable(clusteringAddress, inboundPort)) {
			Exceptions.throwRuntime(
					PlatformException.get(ClusteringError.PORT_IS_ALREADY_IN_USE, inboundPort.toString()));
		}
		ClusterNode.inboundPort = inboundPort;

		Logger.get().info("Using port: " + inboundPort);

		autoScalingEnabled = CloudEnvironment.get().canAutoScale();

		Logger.get().info("Auto Scaling is currently " + (autoScalingEnabled ? "enabled" : "disabled"));

		// Request for Instance/VM Id
		Logger.get().info("Requesting for Instance Id");

		cloudUniqueId = CloudEnvironment.get().autoScaleDelegate().getInstanceId();

		Logger.get().info("Using Instance Id: " + cloudUniqueId);

		// Set WKA

		wka = CloudEnvironment.get().wkaHost();

		Logger.get().info("Using WKA: " + wka.getHostAddress());

		if (false) {
			Scheduler.getDefaultExecutor().scheduleWithFixedDelay((() -> {
				Logger.get().debug(getNodesAsJson());
			}), 1, 10, TimeUnit.SECONDS);
		}

		ClusterFunction.scanAll();

		Logger.get().info("Starting Server Channel..");

		// Setup Server Channel
		Sockets.newServer(ClusterNode.getClusteringAddress(), ClusterNode.getInboundPort());
	}

	static void startWorkflow() {

		// Determine Cluster Join Workflow

		Logger.get().info("Choosing Workflow to use in joining the cluster");

		ClusterJoinWorkflow workflow = CloudEnvironment.get().joinWorkflow();

		if (workflow.getWorkflow() == JoinWorkflow.MASTER) {

			Logger.get().info("Creating Cluster ..");
			MasterNode.start();

		} else {

			Logger.get().info("Requesting to join cluster: " + clusterName());

			new ClientBuilder<NodeJoinRequest, NodeJoinResult>().build(ClusterNode.getWKA(),
					ClusterNode.getInboundPort(), BaseFunction.CLUSTER_JOIN, NodeJoinRequest.get()).thenAccept((r) -> {
						if (r.isSuccess()) {

							Logger.get().info("Node: " + clusteringAddress + " joined the cluster successfully");
							setClusterUniqueId(r.getNodeId());

						} else {
							Logger.get().info(
									"Node: " + clusteringAddress + " could not join cluster because " + r.getMessage());
						}
					});

			// Finally, set role
			ClusterNode.role = Role.SLAVE;

		}

		Logger.get().info("Scanning for Cluster Functions ..");
		ClusterFunction.scanAll();
	}

	/**
	 * This function is used to indicate that this node should be taken out of
	 * service from the cluster
	 */
	public static void stop() {

		Logger.get().info("Stopping Clustering Services");

		nodes.values().forEach((v) -> {
			new ClientBuilder<BaseEvent, Object>().build(IPAddresses.getAddress(v.getRemoteAddress()),
					v.getInboundPort(), BaseFunction.DISPATCH_EVENT,
					new NodeLeaveStartEvent().setNodeId(getClusterUniqueId()));
		});

		nodes.values().forEach((v) -> {
			new ClientBuilder<BaseEvent, Object>().build(IPAddresses.getAddress(v.getRemoteAddress()),
					v.getInboundPort(), BaseFunction.ASYNC_DISPATCH_EVENT,
					new NodeLeaveEvent().setNodeId(getClusterUniqueId()));
		});

		if (channelServer != null) {
			channelServer.close();
		}
	}

	public static Map<String, BaseNodeSpec> nodes = Maps.newHashMap();

	public static String getResourceName(ClusterJoinWorkflow joinWorkflow) {
		String resourceName = AppDelegate.getPlatformPrefix() + "-" + joinWorkflow.toString() + "-" + getNodeCount() + 1;
		return resourceName;
	}

	public static Integer getNodeCount() {
		return ClusterNode.nodes.size();
	}

	public static String getNodesAsJson() {

		StringBuilder sb = new StringBuilder();
		sb.append("\n[");

		List<BaseNodeSpec> v = Lists.newArrayList(nodes.values());

		for (int i = 0; i < v.size(); i++) {

			BaseNodeSpec s = v.get(i);

			sb.append("\n{").append("address: ").append(s.getRemoteAddress() + ":" + s.getInboundPort()).append(",")
					.append("\t role: ").append(s.getRole()).append(",").append("\t state: ").append(s.getState())
					.append(",").append("\t auto: ").append(MasterNode.cloudIdMap.containsKey(s.getId()))

					.append("}");

			if (i < v.size() - 1) {
				sb.append(", ");
			}

		}

		sb.append("\n]");
		return sb.toString();
	}

	static {

	}
}
