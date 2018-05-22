package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.errors.Error;

public class ErrorSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.ERROR;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("Error");
	}
	
	@Override
	ClassIdentityType classIdentity() {
		return ClassIdentityType.DIRECT_SUPER_CLASS;
	}

	@Override
	Class<?> classType() {
		return Error.class;
	}
	
}
