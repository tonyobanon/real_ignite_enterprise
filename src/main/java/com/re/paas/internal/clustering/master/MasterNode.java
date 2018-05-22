package com.re.paas.internal.clustering.master;

import java.util.Map;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.clustering.ClusterNode;
import com.re.paas.internal.clustering.Clustering;
import com.re.paas.internal.clustering.Role;
import com.re.paas.internal.clustering.classes.BaseNodeSpec;
import com.re.paas.internal.clustering.classes.InstanceProfile;
import com.re.paas.internal.clustering.classes.MasterNodeConfig;
import com.re.paas.internal.clustering.classes.NodeState;
import com.re.paas.internal.models.helpers.Dates;
import com.re.paas.internal.utils.Utils;

public class MasterNode {

	protected static InstanceProfile iProfile;
	public static final Map<String, String> cloudIdMap = Maps.newHashMap();

	
	public static void startVM() {

	}
	
	public static void start() {

	
		// Load configuration
		Logger.get().info("Loading master configuration");

		MasterNodeConfig.load(Clustering.APP_BASE + Clustering.MASTER_NODE_CONFIG);




		if (ClusterNode.isAutoScalingEnabled()) {

			/*
			 * Set up Cloud Providers It should be noted that any invalid credentials
			 * (including resource group, subscription, e.t.c) provided, may not throw any
			 * exception until at a later time, after all clustering services have been
			 * started
			 * 
			 */

			// Fetch Instance Profile
			iProfile = CloudEnvironment.get().autoScaleDelegate().getInstanceProfile();
		}

		// Assign clusterUniqueId
		ClusterNode.setClusterUniqueId(Utils.newShortRandom());

		// Set role
		ClusterNode.role = Role.MASTER;
		
		BaseNodeSpec nodeSpec = new BaseNodeSpec()
				.setId(ClusterNode.getClusterUniqueId())
				.setName(ClusterNode.getName())
				.setRemoteAddress(ClusterNode.getClusteringAddress().getHostAddress())
				.setInboundPort(ClusterNode.getInboundPort())
				.setState(NodeState.ONLINE)
				.setRole(Role.MASTER)
				.setJoinDate(Dates.now());
		
		//Add itself to the list of nodes
		ClusterNode.nodes.put(ClusterNode.getClusterUniqueId(), nodeSpec);		
	}
	

}
