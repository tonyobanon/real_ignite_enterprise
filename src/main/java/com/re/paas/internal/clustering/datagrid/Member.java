package com.re.paas.internal.clustering.datagrid;

import java.net.InetAddress;
import java.util.Date;

public class Member {
	
	private final String id;
	private final InetAddress getAddress;
	
	private final Date timeJoined;


	protected Member(String id, InetAddress getAddress) {
		this.id = id;
		this.getAddress = getAddress;
		
		timeJoined = new Date();
	}

	public String getId() {
		return id;
	}

	public InetAddress getGetAddress() {
		return getAddress;
	}

	public Date getTimeJoined() {
		return timeJoined;
	}
	
}
