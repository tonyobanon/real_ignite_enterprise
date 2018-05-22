package com.re.paas.internal.cloud_provider.gae;

import com.re.paas.internal.cloud_provider.AutoScaleDelegate;
import com.re.paas.internal.clustering.classes.ClusterCredentials;
import com.re.paas.internal.clustering.classes.ClusterJoinWorkflow;
import com.re.paas.internal.clustering.classes.InstanceProfile;

public class GaeAutoScaleDelegate extends AutoScaleDelegate {

	@Override
	public String getInstanceId() {
		return com.re.paas.internal.utils.Utils.newShortRandom();
	}

	@Override
	public InstanceProfile getInstanceProfile() {
		return null;
	}

	@Override
	public void startVM(InstanceProfile iProfile, ClusterCredentials auth, ClusterJoinWorkflow joinWorkflow) {
		return;
	}

}
