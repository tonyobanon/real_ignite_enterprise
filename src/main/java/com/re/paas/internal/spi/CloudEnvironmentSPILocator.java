package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.cloud_provider.CloudEnvironment;

public class CloudEnvironmentSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.CLOUD_ENVIRONMENT;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("Environment");
	}

	@Override
	Class<?> classType() {
		return CloudEnvironment.class;
	}
}
