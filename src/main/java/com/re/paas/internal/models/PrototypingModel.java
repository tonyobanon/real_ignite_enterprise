
package com.re.paas.internal.models;

import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.mailing.EmailingModel;

@Model(dependencies = { ActivityStreamModel.class, ApplicationModel.class, BaseUserModel.class, BlobStoreModel.class,
		ConfigModel.class, ConfigurationModel.class, BaseAgentModel.class, EmailingModel.class,
		FormModel.class, LocaleModel.class, LocationModel.class, MetricsModel.class, PlatformModel.class,
		RoleModel.class, SearchModel.class, SystemMetricsModel.class })
public class PrototypingModel implements BaseModel {

	private static final boolean IS_ENABLED = true;

	@Override
	public String path() {
		return "tmp/prototyping";
	}

	@Override
	public void preInstall() {
	}

	@Override
	public void install(InstallOptions options) {

	}

	public static void addMocks() {
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}

}
