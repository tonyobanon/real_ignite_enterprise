package com.re.paas.internal.clustering.classes;

import java.io.Serializable;
import java.util.Date;

import com.re.paas.internal.clustering.Role;

public class BaseNodeSpec implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String id;
	private String name;
	private String remoteAddress;
	private Integer inboundPort;
	private NodeState state;
	private Role role;
	private Date joinDate;

	public String getId() {
		return id;
	}

	public BaseNodeSpec setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}

	public BaseNodeSpec setName(String name) {
		this.name = name;
		return this;
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	public BaseNodeSpec setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
		return this;
	}

	public Integer getInboundPort() {
		return inboundPort;
	}

	public BaseNodeSpec setInboundPort(Integer inboundPort) {
		this.inboundPort = inboundPort;
		return this;
	}

	public NodeState getState() {
		return state;
	}

	public BaseNodeSpec setState(NodeState state) {
		this.state = state;
		return this;
	}

	public Role getRole() {
		return role;
	}

	public BaseNodeSpec setRole(Role role) {
		this.role = role;
		return this;
	}

	public Date getJoinDate() {
		return joinDate;
	}

	public BaseNodeSpec setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
		return this;
	}

}
