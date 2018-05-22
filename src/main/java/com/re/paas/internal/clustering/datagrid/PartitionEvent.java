package com.re.paas.internal.clustering.datagrid;

import java.util.Date;

import com.re.paas.internal.events.BaseEvent;

public class PartitionEvent extends BaseEvent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final String eventId;
	private final String eventDescription;
	
	private final Type type;
	private final Partition partition;
	
	private final Member fromMember;
	private final Member toMember;
	
	private final Date time;


	public PartitionEvent(String eventId, String eventDescription, Type type, Partition partition, Member fromMember,
			Member toMember) {
		
		super();
		
		this.eventId = eventId;
		this.eventDescription = eventDescription;
		
		this.type = type;
		this.partition = partition;
		
		this.fromMember = fromMember;
		this.toMember = toMember;
		this.time = new Date();
	}


	public String getEventId() {
		return eventId;
	}

	public String getEventDescription() {
		return eventDescription;
	}

	public Type getType() {
		return type;
	}

	public Partition getPartition() {
		return partition;
	}

	public Member getFromMember() {
		return fromMember;
	}

	public Member getToMember() {
		return toMember;
	}

	public Date getTime() {
		return time;
	}

	@Override
	public String name() {
		return "PartitionEvent";
	}

	public static enum Type {
		
		PARTITION_TRANSMIT_BEGIN, 
		PARTITION_TRANSIT,
		PARTITION_TRANSMIT_COMMIT, 
		
		PARTITION_TRANSMIT_ROLLBACK
	}
}
