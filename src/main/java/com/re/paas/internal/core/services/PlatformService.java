package com.re.paas.internal.core.services;

import javax.servlet.http.HttpServletResponse;

import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.core.Exceptions;
import com.re.paas.internal.base.core.GsonFactory;
import com.re.paas.internal.core.fusion.WebRoutes;
import com.re.paas.internal.core.fusion.api.BaseService;
import com.re.paas.internal.core.fusion.api.FusionEndpoint;
import com.re.paas.internal.core.users.Functionality;
import com.re.paas.internal.models.PlatformModel;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class PlatformService extends BaseService {
@Override
public String uri() {
	return "/platform/tools";
}
	@FusionEndpoint(uri = "/setup", bodyParams = {
			"payload" }, method = HttpMethod.POST, isBlocking = true, functionality = Functionality.PLATFORM_INSTALLATION)
	public void doSetup(RoutingContext context) {

		try {

			if (PlatformModel.isInstalled()) {
				return;
			}

			JsonObject body = context.getBodyAsJson();

			InstallOptions spec = GsonFactory.getInstance().fromJson(body.getJsonObject("payload").encode(),
					InstallOptions.class);

			// Perform installation
			PlatformModel.doInstall(spec);

			// Go to console
			context.response().putHeader("X-Location", WebRoutes.DEFAULT_CONSOLE_URI)
					.setStatusCode(HttpServletResponse.SC_FOUND);

		} catch (Exception e) {
			Exceptions.throwRuntime(e);
		}
	}

}
