package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.clustering.NodeRole;

public class NodeRoleSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.NODE_ROLE;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("NodeRole");
	}

	@Override
	Class<?> classType() {
		return NodeRole.class;
	}
	
}
