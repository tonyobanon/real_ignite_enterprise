package com.re.paas.internal.cloud_provider.gae;

import java.net.InetAddress;
import java.util.Map;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.classes.FluentHashMap;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.cloud_provider.Tags;
import com.re.paas.internal.clustering.MasterNodeRole;
import com.re.paas.internal.clustering.SlaveNodeRole;
import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.classes.ClusterJoinWorkflow;
import com.re.paas.internal.clustering.classes.JoinWorkflow;
import com.re.paas.internal.clustering.nic_utils.IPAddresses;

/**
 * This class basically provides polyfills to consolidate the GAE Dev environment. This should be used only
 * in development
 * */
public abstract class DevEnvironment extends CloudEnvironment {

	private static final String DEFAULT_MASTER_CLUSTERING_ADDRESS = "1.1.1.0";


	@Override
	public InetAddress httpHost() {
		return IPAddresses.getAddress(getServerAddress());
	}

	@Override
	public InetAddress clusteringHost() {
		return IPAddresses.getAddress(getServerAddress());
	}

	@Override
	public InetAddress wkaHost() {
		return IPAddresses.getAddress(DEFAULT_MASTER_CLUSTERING_ADDRESS);
	}

	@Override
	public Integer httpPort() {
		if(joinWorkflow().getWorkflow() != JoinWorkflow.MASTER) {
			return Integer.parseInt(getServerAddress().replace(".", "").trim());
		} else {
			return super.httpPort();
		}
	}

	private String getServerAddress() {
		String serverAddress = System.getProperty("sun.java.command").split("--")[1].replace("address=", "").replaceFirst("/", "").trim();
		return serverAddress;
	}
	
	@Override
	public ClusterCredentials credentials() {
		return new ClusterCredentials("a", "a");
	}

	@Override
	public Map<String, String> getInstanceTags() {
		String role = getServerAddress().equals(DEFAULT_MASTER_CLUSTERING_ADDRESS) ? new MasterNodeRole().names().iterator().next() : new SlaveNodeRole().names().iterator().next();
		return FluentHashMap.forNameMap().with(Tags.NODE_ROLE_TAG, role);
	}
	
}
