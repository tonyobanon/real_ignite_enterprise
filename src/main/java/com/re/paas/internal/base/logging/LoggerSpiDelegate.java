package com.re.paas.internal.base.logging;

import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.utils.ClassUtils;

public class LoggerSpiDelegate extends SpiDelegate<Logger> {
	
	@Override
	protected void init() {
		get(c -> {
			set(DEFAULT_NAMESPACE, ClassUtils.createInstance(c));
		});
	}
	
}
