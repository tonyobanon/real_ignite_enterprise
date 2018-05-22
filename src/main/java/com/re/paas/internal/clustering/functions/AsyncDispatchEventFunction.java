package com.re.paas.internal.clustering.functions;

import com.re.paas.internal.clustering.ClusterFunction;
import com.re.paas.internal.clustering.Role;
import com.re.paas.internal.events.BaseEvent;
import com.re.paas.internal.events.EventBus;

public class AsyncDispatchEventFunction extends ClusterFunction<BaseEvent, Object> {

	@Override
	public Role[] roles() {
		return Role.values();
	}

	@Override
	public BaseFunction id() {
		return BaseFunction.ASYNC_DISPATCH_EVENT;
	}

	@Override
	public Object delegate(BaseEvent t) {
		EventBus.dispatch(t, true);
		return null;
	}

}
