package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.templating.api.TemplateObjectModel;

public class TemplateObjectModelSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.TEMPLATE_OBJECT_MODEL;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("Template");
	}

	@Override
	ClassIdentityType classIdentity() {
		return ClassIdentityType.ASSIGNABLE_FROM;
	}
	
	@Override
	Class<?> classType() {
		return TemplateObjectModel.class;
	}
	
}
