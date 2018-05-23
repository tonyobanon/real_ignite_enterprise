package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.ServiceDelegate;

public class ServiceSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.SERVICE;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("Service");
	}

	@Override
	Class<?> classType() {
		return BaseService.class;
	}
	
	@Override
	Class<?> delegateType() {
		return ServiceDelegate.class;
	}
	
}
