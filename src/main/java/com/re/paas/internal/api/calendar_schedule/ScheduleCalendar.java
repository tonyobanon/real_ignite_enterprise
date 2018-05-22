package com.re.paas.internal.api.calendar_schedule;

import java.util.List;

import com.google.common.collect.Lists;

public class ScheduleCalendar {

	List<CalendarMonth> months = Lists.newArrayList();

	public List<CalendarMonth> getMonths() {
		return months;
	}

	public ScheduleCalendar addMonth(CalendarMonth month) {
		this.months.add(month);
		return this;
	}
	
	public ScheduleCalendar setMonths(List<CalendarMonth> months) {
		this.months = months;
		return this;
	}
	
}
