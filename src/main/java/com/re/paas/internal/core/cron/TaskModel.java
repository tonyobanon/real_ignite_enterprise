package com.re.paas.internal.core.cron;

import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.TaskExecutionOutcome;
import com.re.paas.internal.core.forms.Question;

public abstract class TaskModel implements Cloneable {
	
	private Map<String, String> parameters;
	
	public abstract String name();
	
	public abstract ClientRBRef title();
	
	public abstract List<Question> fields();
	
	public Map<String, String> getParameters() {
		return parameters;
	}

	public TaskModel setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}

	public abstract TaskExecutionOutcome call();
	
}
