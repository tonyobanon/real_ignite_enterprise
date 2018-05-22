package com.re.paas.internal.models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.common.collect.Lists;
import com.re.paas.internal.api.calendar_schedule.CalendarMonth;
import com.re.paas.internal.api.calendar_schedule.ScheduleCalendar;
import com.re.paas.internal.base.classes.InstallOptions;
import com.re.paas.internal.base.classes.spec.PropertyViewRequestStatus;
import com.re.paas.internal.base.classes.spec.PublicHolidaySpec;
import com.re.paas.internal.base.core.PlatformException;
import com.re.paas.internal.entites.directory.PropertyEntity;
import com.re.paas.internal.errors.RexError;

public class PropertyModel implements BaseModel {

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	public String path() {
		return "core/property";
	}

	public static void newPropertyViewRequest(Long userId, Long propertyId, Date viewingDate) {

		PropertyEntity pe = BasePropertyModel.getProperty(propertyId);

		// Verify that the said time is not a holiday in the country of the
		// agentOrganization
		if (HolidayModel.getHoliday(pe.getCountry(), viewingDate) != null) {
			throw new PlatformException(RexError.THE_SPECIFIED_DATE_IS_A_HOLIDAY);
		}

		// get available agents, verify that at least the agents are available at the
		// said time

		// Send SMS to selected agent's phone

		// Add to activity stream

	}

	public static void isAgentAvailable(Long agent, Date viewingDate) {

	}

	public static void updatePropertyViewRequestStatus(PropertyViewRequestStatus status, String statusMessage) {

		// Add to activity stream

	}

	public static void getScheduleCalendar(Long agentOrganization) {

		ScheduleCalendar sc = new ScheduleCalendar();

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Locale locale = Locale.getDefault();// here, get user's locale

		String country = "US"; // get country of property's location

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_MONTH, 1);

		for (int i = 0; i < 60; i++) {

			calendar.add(Calendar.DAY_OF_MONTH, 1);

			CalendarMonth month = new CalendarMonth(calendar.get(Calendar.DAY_OF_MONTH),
					calendar.getDisplayName(Calendar.DAY_OF_MONTH, Calendar.LONG_FORMAT, locale));

			// Should be a working day

			int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
				// unavailable
			}

			// Should not be a public holiday in the property's country
			PublicHolidaySpec hs = HolidayModel.getHoliday(country, calendar.getTime());
			if (hs == null) {
				// unavailable
			}

			System.out.println(dateFormat.format(calendar.getTime()));

			int week = calendar.get(Calendar.WEEK_OF_MONTH);
			int day = calendar.get(Calendar.DAY_OF_WEEK);

			List<Long> agents = Lists.newArrayList();

		}

	}

	@Override
	public void install(InstallOptions options) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preInstall() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unInstall() {
		// TODO Auto-generated method stub
		
	}

}
