package com.re.paas.internal.clustering.events;

import com.re.paas.internal.events.BaseEvent;

import io.netty.channel.Channel;

public class NodeServerStartEvent extends BaseEvent {

	private static final long serialVersionUID = 1L;
	private final Channel channel;
	
	
	public NodeServerStartEvent(Channel channel) {
		super();
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}
	
	@Override
	public String name() {
		return "ServerStartEvent";
	}
}
