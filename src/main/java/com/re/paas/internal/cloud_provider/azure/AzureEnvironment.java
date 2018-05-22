package com.re.paas.internal.cloud_provider.azure;

import java.net.InetAddress;
import java.util.Map;

import javax.cache.CacheManager;

import com.re.paas.internal.cloud_provider.AutoScaleDelegate;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.core.storageapi.DatabaseStorage;

public class AzureEnvironment extends CloudEnvironment {

	@Override
	public String id() {
		return "azure";
	}

	@Override
	public boolean isProduction() {
		return true;
	}

	@Override
	public String title() {
		return "Azure";
	}

	@Override
	public InetAddress wkaHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean canAutoScale() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DatabaseStorage storage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> getInstanceTags() {
		return AzureHelper.getInstanceTags();
	}

	@Override
	public AutoScaleDelegate autoScaleDelegate() {
		// TODO Auto-generated method stub
		return null;
	}
}
