package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.clustering.ClusterFunction;

public class ClusterFunctionSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.CLUSTER_FUNCTION;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("Function");
	}

	@Override
	Class<?> classType() {
		return ClusterFunction.class;
	}
	
}
