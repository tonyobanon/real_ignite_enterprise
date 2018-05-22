package com.re.paas.internal.api.calendar_schedule;

import java.util.List;

import com.google.common.collect.Lists;

public class CalendarMonth {

	private Integer value;
	private String name;
	
	private List<CalendarDay> days = Lists.newArrayList();

	
	public CalendarMonth(Integer value, String name) {
		this.value = value;
		this.name = name;
	}

	public Integer getValue() {
		return value;
	}

	public CalendarMonth setValue(Integer value) {
		this.value = value;
		return this;
	}

	public String getName() {
		return name;
	}

	public CalendarMonth setName(String name) {
		this.name = name;
		return this;
	}

	public List<CalendarDay> getDays() {
		return days;
	}

	public CalendarMonth addDay(CalendarDay day) {
		this.days.add(day);
		return this;
	}
	
	public CalendarMonth setDays(List<CalendarDay> days) {
		this.days = days;
		return this;
	}
	
}
