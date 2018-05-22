package com.re.paas.internal.clustering.functions;

import com.re.paas.internal.clustering.ClusterFunction;
import com.re.paas.internal.clustering.Role;

public class PingFunction extends ClusterFunction<String, String> {

	@Override
	public Role[] roles() {
		return Role.values();
	}

	@Override
	public BaseFunction id() {
		return BaseFunction.PING;
	}

	@Override
	public String delegate(String t) {
		return "Hello " + t;
	}

}
