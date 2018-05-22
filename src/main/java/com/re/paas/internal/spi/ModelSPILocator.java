package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.models.BaseModel;

public class ModelSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.MODEL;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("Model");
	}

	@Override
	Class<?> classType() {
		return BaseModel.class;
	}
	
}
