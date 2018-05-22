package com.re.paas.internal.cloud_provider;

import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.utils.ClassUtils;

public class CloudEnvironmentSpiDelegate extends SpiDelegate<CloudEnvironment>{

	@Override
	protected void init() {
		get(c -> {
			CloudEnvironment e = ClassUtils.createInstance(c);
			if (e.enabled()) {
				set(DEFAULT_NAMESPACE, e);
			}
		});
	}

}
