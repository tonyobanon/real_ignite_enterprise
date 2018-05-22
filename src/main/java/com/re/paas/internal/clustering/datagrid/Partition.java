package com.re.paas.internal.clustering.datagrid;

import java.util.Date;

public class Partition {

	private final String id;

	private Long currentSize;
	private final Long maxSize;

	private Status status;
	
	private Member location;

	private final Date timeCreated;
	private Date timeModified;

	
	protected Partition(String id, Long maxSize) {
		
		this.id = id;
		this.currentSize = 0L;
		this.maxSize = maxSize;
		this.timeCreated = new Date();
		this.timeModified = new Date();
		
	}

	public String getId() {
		return id;
	}

	public Long getCurrentSize() {
		return currentSize;
	}

	public Long getMaxSize() {
		return maxSize;
	}
	
	public void addSize(Long size){
		currentSize += size;
	}

	public Member getLocation() {
		return location;
	}

	public Date getTimeCreated() {
		return timeCreated;
	}

	public Date getTimeModified() {
		return timeModified;
	}

	public Status getStatus() {
		return status;
	}
	
	public Partition withLocation(Member member) {
		this.location = member;
		return this;
	}
	
	protected Partition withStatus(Status status) {
		this.status = status;
		return this;
	}
	

	public static enum Status {
		
		PARTITION_PARTIALLY_AVAILABLE,
		PARTITION_LOST,
		
		PARTITION_AVAILABLE,
		PARTITION_IN_TRANSIT
	}

}
