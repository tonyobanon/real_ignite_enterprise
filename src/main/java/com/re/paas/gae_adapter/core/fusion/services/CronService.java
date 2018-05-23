package com.re.paas.gae_adapter.core.fusion.services;

import com.re.paas.gae_adapter.core.fusion.CronServiceAuthenticator;
import com.re.paas.internal.base.classes.CronInterval;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.CronModel;

import io.vertx.ext.web.RoutingContext;

public class CronService extends BaseService {
	@Override
	public String uri() {
		return "/cronservice";
	}

	@FusionEndpoint(uri = "/executeAll", isBlocking = true, functionality = Functionality.EXECUTE_CRON_JOBS, customAuthenticator = CronServiceAuthenticator.class, requestParams = "interval")
	public void executeAll(RoutingContext ctx) {
		CronInterval interval = CronInterval.from(Integer.parseInt(ctx.request().getParam("interval")));
		CronModel.executeAll(interval);
	}
}
