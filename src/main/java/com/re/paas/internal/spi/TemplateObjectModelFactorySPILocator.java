package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.templating.api.TemplateObjectModelFactory;

public class TemplateObjectModelFactorySPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.TEMPLATE_OBJECT_MODEL_FACTORY;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("TemplateFactory");
	}

	@Override
	Class<?> classType() {
		return TemplateObjectModelFactory.class;
	}

	
}
