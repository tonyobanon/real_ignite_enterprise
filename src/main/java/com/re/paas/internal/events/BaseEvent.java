package com.re.paas.internal.events;

import java.io.Serializable;

public abstract class BaseEvent implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private String eventId;
	
	
	protected BaseEvent() {
	}

	public String getEventId() {
		return eventId;
	}
	
	public BaseEvent setEventId(String eventId) {
		this.eventId = eventId;
		return this;
	}

	public abstract String name();
	
}
