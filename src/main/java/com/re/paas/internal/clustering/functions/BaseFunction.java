package com.re.paas.internal.clustering.functions;

import com.re.paas.internal.clustering.Function;

public enum BaseFunction implements Function {

	PING(0, false), DISPATCH_EVENT(1, false), ASYNC_DISPATCH_EVENT(2, true), CLUSTER_JOIN(3, false);

	private short value;
	private boolean isAsync;

	private BaseFunction(int value, boolean isAsync) {
		this.value = (short) value;
		this.isAsync = isAsync;
	}

	public static BaseFunction from(int value) {

		switch (value) {
		
		case 3:
			return BaseFunction.CLUSTER_JOIN;
		
		case 2:
			return BaseFunction.ASYNC_DISPATCH_EVENT;
			
		case 1:
			return BaseFunction.DISPATCH_EVENT;
			
		case 0:
			return BaseFunction.PING;

		default:
			throw new IllegalArgumentException("An invalid value was provided");
		}
	}

	public short getValue() {
		return value;
	}
	
	@Override
	public boolean isAsync() {
		return isAsync;
	}
}
