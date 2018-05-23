package com.re.paas.internal.core.fusion;

import com.re.paas.internal.core.fusion.api.ServiceAuthenticator;

import io.vertx.ext.web.RoutingContext;

public class DefaultServiceAuthenticator implements ServiceAuthenticator{

	@Override
	public boolean authenticate(RoutingContext ctx) {
		return true;
	}

}
