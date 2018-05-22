package com.re.paas.internal.core.services;

import java.util.List;
import java.util.Map;

import com.re.paas.internal.base.classes.ClientRBRef;
import com.re.paas.internal.base.classes.CronInterval;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.cron.ModelTask;
import com.re.paas.internal.core.forms.Question;
import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.fusion.FusionHelper;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.CronModel;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class CronService extends BaseService{
@Override
public String uri() {
	return "/cron-service";
}
	@FusionEndpoint(uri = "/get-cron-task-model-names", requestParams = {}, method = HttpMethod.GET, functionality = Functionality.GET_CRON_TASK_MODEL)
	public void getCronTaskModelNames(RoutingContext ctx) {
		Map<String, ClientRBRef> result = CronModel.getTaskModelNames();
		FusionHelper.response(ctx, result);
	}

	@FusionEndpoint(uri = "/get-cron-task-model-fields", requestParams = {
			"name" }, method = HttpMethod.GET, functionality = Functionality.GET_CRON_TASK_MODEL)
	public void getCronTaskModelFields(RoutingContext ctx) {

		String name = ctx.request().getParam("name");

		List<Question> result = CronModel.getTaskModelFields(name);
		FusionHelper.response(ctx, result);
	}

	@FusionEndpoint(uri = "/create-cron-task", bodyParams = { "name", "interval", "task",
			"maxExecutionCount" }, method = HttpMethod.PUT, functionality = Functionality.CREATE_CRON_TASK)
	public void newCronTask(RoutingContext ctx) {

		JsonObject body = ctx.getBodyAsJson();

		String name = body.getString("name");
		CronInterval interval = CronInterval.from(body.getInteger("interval"));

		ModelTask task = GsonFactory.getInstance().fromJson(body.getJsonObject("task").encode(), ModelTask.class);

		Integer maxExecutionCount = body.getInteger("maxExecutionCount");

		Long id = CronModel.newCronTask(name, interval, task, maxExecutionCount, false);
		FusionHelper.response(ctx, id);
	}

	@FusionEndpoint(uri = "/delete-cron-task", requestParams = {
			"id" }, method = HttpMethod.DELETE, functionality = Functionality.DELETE_CRON_TASK)
	public void deleteCronTask(RoutingContext ctx) {

		Long id = Long.parseLong(ctx.request().getParam("id"));

		CronModel.deleteCronTask(id);
	}

}
