package com.re.paas.internal.clustering;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.cloud_provider.Tags;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;
import com.re.paas.internal.utils.ClassUtils;

public abstract class NodeRole {

	private static Map<String, NodeRole> nodeRoles = Collections.synchronizedMap(new HashMap<String, NodeRole>());

	public static void scanAll() {

		new SpiDelegate<NodeRole>(SpiTypes.NODE_ROLE).get(c -> {

					NodeRole o = ClassUtils.createInstance(c);
					o.names().forEach(n -> {

						if (nodeRoles.containsKey(n)) {
							//o.logOverrideWarning();
						}

						nodeRoles.put(n, o);
					});
				});
	}

	public abstract Iterable<String> names();

	public static NodeRole get() {
		String role = CloudEnvironment.get().getInstanceTags().get(Tags.NODE_ROLE_TAG);
		return nodeRoles.get(role);
	}

	public abstract void start();

	public abstract void stop();

}
