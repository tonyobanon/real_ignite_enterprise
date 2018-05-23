package com.re.paas.internal.core.cron.taskmodels;

import java.util.List;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.FluentArrayList;
import com.re.paas.internal.base.classes.TaskExecutionOutcome;
import com.re.paas.internal.core.cron.TaskModel;
import com.re.paas.internal.core.forms.CompositeEntry;
import com.re.paas.internal.core.forms.Question;
import com.re.paas.internal.core.fusion.FusionServiceDelegate;
import com.re.paas.internal.core.users.Functionality;

public class RbTranslateTaskModel extends TaskModel {

	@Override
	public String name() {
		return "task_model/rb_translate";
	}

	@Override
	public ClientRBRef title() {
		return ClientRBRef.get("resource_bundle_translation_task");
	}

	@Override
	public List<Question> fields() {
		return new FluentArrayList<Question>().with(new CompositeEntry("target_country", ClientRBRef.get("country"))
				.setItemsSource(FusionServiceDelegate.getFunctionalityRoute(Functionality.GET_COUNTRY_NAMES).get(0)).setSortOrder(1)
				.setIsDefault(true));
	}

	@Override
	public TaskExecutionOutcome call() {

		String country = getParameters().get("target_country");

		// Do some stuff

		return TaskExecutionOutcome.SUCCESS;
	}

}
