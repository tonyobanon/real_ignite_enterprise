package com.re.paas.internal.api.calendar_schedule;

import java.util.Map;

import com.google.common.collect.Maps;
import com.re.paas.internal.base.classes.ClientRBRef;

public class CalendarDay {

	private String name;
	private boolean isAvailable;
	private ClientRBRef comment;
	private Map<String, String> times = Maps.newHashMap();

	public String getName() {
		return name;
	}

	public CalendarDay setName(String name) {
		this.name = name;
		return this;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public CalendarDay setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
		return this;
	}

	public ClientRBRef getComment() {
		return comment;
	}

	public CalendarDay setComment(ClientRBRef comment) {
		this.comment = comment;
		return this;
	}

	public Map<String, String> getTimes() {
		return times;
	}
	
	public CalendarDay addTime(String id, String time) {
		this.times.put(id, time);
		return this;
	}

	public CalendarDay setTimes(Map<String, String> times) {
		this.times = times;
		return this;
	}
}
