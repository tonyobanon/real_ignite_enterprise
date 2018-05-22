package com.re.paas.internal.models.helpers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.re.paas.internal.base.core.BlockerTodo;

@BlockerTodo("Here and global.js, stop using the system's default timezone offset. Instead use platform configured timezone")
public class Dates {
	
	private static DateFormat prettyFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	@BlockerTodo("Save using the platform's default timezone")
	public static Date now(){
		return new Date();
	}
	
	public static Calendar getCalendar(){
		return Calendar.getInstance();
	}
	
	public static String currentDate(){
		return format.format(getCalendar().getTime());
	}
	
	public static String toString(Date o) {
		return format.format(o);
	}
	
	public static String toPrettyString(Date o) {
		return prettyFormat.format(o);
	}

	public static Date toDate(String o) throws ParseException {
			return format.parse(o);
	}
	
	static {
		
	}
	
}
