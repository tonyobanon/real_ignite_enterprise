package com.re.paas.internal.clustering.events;

import com.re.paas.internal.events.BaseEvent;

import io.netty.channel.Channel;

public class NodeServerStopEvent extends BaseEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Channel channel;
	
	public NodeServerStopEvent(Channel channel) {
		super();
		this.channel = channel;
	}

	public Channel getChannel() {
		return channel;
	}
	
	@Override
	public String name() {
		return "ServerStopEvent";
	}
	
}
