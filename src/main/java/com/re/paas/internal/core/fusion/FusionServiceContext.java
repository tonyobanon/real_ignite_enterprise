package com.re.paas.internal.core.fusion;

import java.lang.reflect.Method;

public class FusionServiceContext {

	private final BaseService service;
	private final FusionEndpoint endpoint;
	private final Method method;
	private final boolean isClassEnd;
	
	public FusionServiceContext(BaseService service, FusionEndpoint endpoint, Method method, boolean isClassEnd) {
		this.service = service;
		this.endpoint = endpoint;
		this.method = method;
		this.isClassEnd = isClassEnd;
	}

	public BaseService getService() {
		return service;
	}

	public FusionEndpoint getEndpoint() {
		return endpoint;
	}

	public Method getMethod() {
		return method;
	}

	public boolean isClassEnd() {
		return isClassEnd;
	}
	
}
