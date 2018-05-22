package com.re.paas.internal.cloud_provider.gae;

import java.util.Map;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.collect.Maps;
import com.re.paas.internal.cloud_provider.AutoScaleDelegate;
import com.re.paas.internal.core.storageapi.DatabaseStorage;

public class GaeEnvironment extends DevEnvironment {

	@Override
	public String id() {
		return "gae";
	}

	@Override
	public boolean enabled() {
		return true;
	}
	
	@Override
	public boolean isStandalone() {
		return false;
	}
	
	@Override
	public boolean isProduction() {
		return SystemProperty.environment.value() == SystemProperty.Environment.Value.Production;
	}

	@Override
	public String title() {
		return "Google App Engine";
	}
	
	@Override
	public Boolean canAutoScale() {
		return false;
	}

	@Override
	public DatabaseStorage storage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AutoScaleDelegate autoScaleDelegate() {
		return new GaeAutoScaleDelegate();
	}

}
