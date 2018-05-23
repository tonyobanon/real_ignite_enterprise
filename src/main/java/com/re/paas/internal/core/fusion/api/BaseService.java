package com.re.paas.internal.core.fusion.api;

import com.re.paas.internal.base.core.BlockerTodo;
import com.re.paas.internal.core.fusion.FusionHelper;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.BaseModel;
import com.re.paas.internal.models.BaseUserModel;
import com.re.paas.internal.models.RoleModel;
import com.re.paas.internal.spi.SpiDelegate;
import com.re.paas.internal.spi.SpiTypes;

import io.vertx.ext.web.RoutingContext;

@BlockerTodo("Add helper method here to check param instead of always checking for undefined")
public abstract class BaseService {

	public abstract String uri();

	@SuppressWarnings("unchecked")
	public Class<? extends BaseModel>[] externalModels() {
		return new Class[] {};
	}

	protected String getLocationHeader() {
		return "X-Location";
	}

	public void isAccessAllowed(RoutingContext ctx, Functionality... functionalities) {

		Long principal = FusionHelper.getUserId(ctx.request());
		String roleName = BaseUserModel.getRole(principal);

		Boolean b = RoleModel.isAccessAllowed(roleName, functionalities);

		ctx.response().write(b.toString()).setChunked(true);
	}

	public static ServiceDelegate getDelegate() {
		@SuppressWarnings("unchecked")
		SpiDelegate<BaseService> delegate = (SpiDelegate<BaseService>) SpiDelegate.getDelegate(SpiTypes.SERVICE);
		return (ServiceDelegate) delegate;
	}

}
