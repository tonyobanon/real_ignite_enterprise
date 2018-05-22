package com.re.paas.internal.cloud_provider;

import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.classes.ClusterJoinWorkflow;
import com.re.paas.internal.clustering.classes.InstanceProfile;

public abstract class AutoScaleDelegate {

	public abstract String getInstanceId();

	public abstract InstanceProfile getInstanceProfile();

	public abstract void startVM(InstanceProfile iProfile, ClusterCredentials auth, ClusterJoinWorkflow joinWorkflow);

}
