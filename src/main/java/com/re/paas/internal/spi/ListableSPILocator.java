package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.base.core.Listable;

public class ListableSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.LISTABLE;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("List");
	}

	@Override
	Class<?> classType() {
		return Listable.class;
	}
	
}
