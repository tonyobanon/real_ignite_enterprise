package com.re.paas.internal.clustering.datagrid;

import com.re.paas.internal.events.BaseEvent;

public class MemberEvent extends BaseEvent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String eventId;
	private final String eventDescription;
	
	private final Member member;

	protected MemberEvent(String eventId, String eventDescription, Member member) {
		super();
		this.eventId = eventId;
		this.eventDescription = eventDescription;
		this.member = member;
	}

	public String getEventId() {
		return eventId;
	}

	public Member getMember() {
		return member;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	@Override
	public String name() {
		return "MemberEvent";
	}
	
}
