package com.re.paas.internal.spi;

public enum SpiTypes {

	LOGGER(1), CLOUD_ENVIRONMENT(1),ERROR, NODE_ROLE, EVENT_LISTENER, MODEL, CLUSTER_FUNCTION, TEMPLATE_OBJECT_MODEL, TEMPLATE_OBJECT_MODEL_FACTORY, TASK_MODEL, LISTABLE, SERVICE;

	private final int count;

	private SpiTypes() {
		this(-1);
	}

	private SpiTypes(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

}
