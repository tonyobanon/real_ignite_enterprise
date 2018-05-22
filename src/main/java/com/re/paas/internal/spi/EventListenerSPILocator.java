package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.events.EventListener;

public class EventListenerSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.EVENT_LISTENER;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("EventListener");
	}

	@Override
	ClassIdentityType classIdentity() {
		return ClassIdentityType.ANNOTATION;
	}
	
	@Override
	Class<?> classType() {
		return EventListener.class;
	}

	
}
