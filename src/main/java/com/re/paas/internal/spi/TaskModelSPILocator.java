package com.re.paas.internal.spi;

import com.google.common.collect.Lists;
import com.re.paas.internal.core.cron.TaskModel;

public class TaskModelSPILocator extends BaseSPILocator {

	@Override
	SpiTypes spiType() {
		return SpiTypes.TASK_MODEL;
	}

	@Override
	public Iterable<String> classSuffix() {
		return Lists.newArrayList("TaskModel");
	}
	
	@Override
	Class<?> classType() {
		return TaskModel.class;
	}
}
