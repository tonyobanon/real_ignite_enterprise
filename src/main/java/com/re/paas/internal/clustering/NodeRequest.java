package com.re.paas.internal.clustering;

public class NodeRequest extends BaseRequest {

	private static final long serialVersionUID = 1L;
	
	private String remoteAddress;
	
	public String getRemoteAddress() {
		return remoteAddress;
	}

	NodeRequest setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}
	
}
