package com.re.paas.internal.clustering;

import com.re.paas.internal.base.core.DefaultLogger;
import com.re.paas.internal.base.logging.Logger;
import com.re.paas.internal.clustering.classes.BaseNodeSpec;
import com.re.paas.internal.clustering.events.NodeJoinEvent;
import com.re.paas.internal.clustering.events.NodeJoinStartEvent;
import com.re.paas.internal.clustering.events.NodeServerStartEvent;
import com.re.paas.internal.clustering.events.NodeStateChangeEvent;
import com.re.paas.internal.events.EventListener;
import com.re.paas.internal.events.Subscribe;

@EventListener
public class NodeEventListener {

	@Subscribe
	public void onServerStart(NodeServerStartEvent evt) {
		ClusterNode.channelServer = evt.getChannel();
		Logger.get().info("Socket server started on: " + evt.getChannel().localAddress());
		ClusterNode.startWorkflow();
	}

	@Subscribe
	public void onNodeJoin(NodeJoinEvent evt) {

		for (BaseNodeSpec spec : evt.getSpec()) {
			ClusterNode.nodes.put(spec.getId(), spec);
			Logger.get().debug("NodeJoinEvent -> event id: " + evt.getEventId() + " id: " + spec.getId() + " address: "
					+ spec.getRemoteAddress());
		}
	}

	@Subscribe
	public void onNodeJoining(NodeJoinStartEvent evt) {

	}

	@Subscribe
	public void onNodeLeaveStart() {

		switch (ClusterNode.role) {
		case MASTER:

			// Stop all instances
			// Stop this instance

			break;
		case SLAVE:

			// Request that master put this instance out of service

			// This may involve:
			// Moving partitions elsewhere, e.t.c
			// The master notifies other members of its departure

			Logger.get().info("Stopping Clustering Services for Node: " + ClusterNode.getClusterUniqueId() + "..");
			Logger.get().stop();

			break;
		}
	}

	@Subscribe
	public void onNodeStateChange(NodeStateChangeEvent evt) {
		ClusterNode.nodes.get(evt.getNodeId()).setState(evt.getNewState());
		Logger.get().debug("NodeStateChangeEvent -> event id: " + evt.getEventId() + " id: " + evt.getNodeId()
				+ " new state: " + evt.getNewState().name());
	}

}
