package com.re.paas.gae_adapter.core.fusion;

import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.core.fusion.api.ServiceAuthenticator;

import io.vertx.ext.web.RoutingContext;

public class CronServiceAuthenticator implements ServiceAuthenticator {

	private static final String GAE_CRON_HEADER = "X-Appengine-Cron";
	private static final String GAE_CRON_REMOTE_ADDRESS = "0.1.0.1";

	/**
	 * This custom authenticator is used to verify that all requests that hits our
	 * cron service is triggered internally by the GAE cron service
	 */
	@Override
	public boolean authenticate(RoutingContext ctx) {
		if (CloudEnvironment.get().isProduction()) {
			return ctx.request().getHeader(GAE_CRON_HEADER).equals("true")
					&& ctx.request().remoteAddress().host().equals(GAE_CRON_REMOTE_ADDRESS);
		} else {
			return true;
		}
	}

}
