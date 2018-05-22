package com.re.paas.internal.core.services;

import com.re.paas.internal.core.fusion.BaseService;
import com.re.paas.internal.core.fusion.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.PrototypingModel;

import io.vertx.ext.web.RoutingContext;

public class PrototypingService extends BaseService {
@Override
public String uri() {
	return "/prototyping";
}
	@FusionEndpoint(uri = "/create-mocks", functionality = Functionality.ADD_SYSTEM_MOCK_DATA)
	public void createMocks(RoutingContext ctx) {
		PrototypingModel.addMocks();
	}
	
}
