package com.re.paas.internal.cloud_provider;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.Map;

import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.clustering.classes.ClusterConfig;
import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.nic_utils.IPAddresses;
import com.re.paas.internal.clustering.nic_utils.NIC;
import com.re.paas.internal.core.storageapi.DatabaseStorage;
import com.re.paas.internal.errors.ApplicationError;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;
import com.re.paas.internal.utils.Utils;

public abstract class CloudEnvironment {
	
	public static CloudEnvironment get() {
		return (CloudEnvironment) SpiDelegate.getDelegate(SpiTypes.CLOUD_ENVIRONMENT).get();
	}
	
	public abstract String id();

	public boolean enabled() {
		return false;
	}

	/**
	 * This is used to determine if the platform is running within a servlet
	 * container, or as a standalone java application
	 */
	public boolean isStandalone() {
		return true;
	}

	public abstract boolean isProduction();

	public abstract String title();

	public InetAddress httpHost() {

		// Use a public network address (if available)
		for (InetAddress address : NIC.getAllAddress()) {
			if (NIC.isInetAddressPublic(address)) {
				return address;
			}
		}

		// Use a private network address instead
		for (InetAddress address : NIC.getAllAddress()) {
			if (NIC.isInetAddressPrivate(address)) {
				return address;
			}
		}

		return (InetAddress) Exceptions
				.throwRuntime(PlatformException.get(ApplicationError.COULD_NOT_FIND_SUITABLE_HTTP_ADDRESS));
	}

	public Integer httpPort() {
		return ClusterConfig.getInteger(ClusterConfig.HTTP_PORT);
	}

	public InetAddress clusteringHost() {

		// Use a private network address
		for (InetAddress address : NIC.getAllAddress()) {
			if (NIC.isInetAddressPrivate(address)) {
				
				if (address instanceof Inet6Address) {
					return (InetAddress) Exceptions.throwRuntime(new RuntimeException("IPv6 addresses not supported for clustering"));
				}
				
				return address;
			}
		}

		return (InetAddress) Exceptions
				.throwRuntime(PlatformException.get(ApplicationError.COULD_NOT_FIND_SUITABLE_CLUSTERING_ADDRESS));
	}

	public final Integer clusteringPort() {
		return ClusterConfig.getInteger(ClusterConfig.CLUSTERING_PORT);
	}

	public abstract Boolean canAutoScale();

	public abstract DatabaseStorage storage();

	public InetAddress wkaHost() {
		return IPAddresses.getAddress(getInstanceTags().get(Tags.WKA_HOST_TAG));
	}

	public abstract Map<String, String> getInstanceTags();

	public ClusterCredentials credentials() {
		
		if(joinWorkflow().getWorkflow() == JoinWorkflow.MASTER) {
			return new ClusterCredentials(Utils.newShortRandom(), Utils.newShortRandom());
		} else {
			
			String accessKey = getInstanceTags().get(ClusterCredentials.CLUSTER_ACCESS_KEY_TAG);
			String secretKey = getInstanceTags().get(ClusterCredentials.CLUSTER_SECRET_KEY_TAG);
			
			return new ClusterCredentials(accessKey, secretKey);
		}
	}
	
	public abstract AutoScaleDelegate autoScaleDelegate();

}
