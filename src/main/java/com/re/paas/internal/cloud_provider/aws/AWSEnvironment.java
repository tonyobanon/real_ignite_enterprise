package com.re.paas.internal.cloud_provider.aws;

import java.net.InetAddress;
import java.util.Map;

import com.re.paas.internal.cloud_provider.AutoScaleDelegate;
import com.re.paas.internal.cloud_provider.CloudEnvironment;
import com.re.paas.internal.core.storageapi.DatabaseStorage;

public class AWSEnvironment extends CloudEnvironment {

	@Override
	public String id() {
		return null;
	}

	@Override
	public boolean isProduction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String title() {
		// TODO Auto-generated method stub
		return null;
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
	
	/**
	 * In AWS, we use user-data to store our "instance tags"
	 * */	
	@Override
	public Map<String, String> getInstanceTags() {
		return AWSHelper.getUserData();
	}

	@Override
	public AutoScaleDelegate autoScaleDelegate() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
