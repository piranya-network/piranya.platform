package network.piranya.platform.api.utilities;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import network.piranya.platform.api.exceptions.PiranyaException;
import network.piranya.platform.api.lang.TimeWindow;
import network.piranya.platform.api.lang.TimeWindowUnit;

public class DateTimeUtils {
	
	public String format(long time, String format) {
		return new SimpleDateFormat(format).format(time);
	}
	
	public long parse(String str, String format) {
		try {
			return new SimpleDateFormat(format).parse(str).getTime();
		} catch (ParseException ex) {
			throw new PiranyaException(String.format("Failed to parse date-time '%s' of format '%s': %s", str, format, ex.getMessage()));
		}
	}
	
	public TimeWindow calcTimeWindow(long now, TimeWindowUnit unit, long interval) {
		if (unit.inMillis() >= TimeWindowUnit.WEEK.inMillis()) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(now);
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			int calUnit = toCalendarField(unit);
			int initialCalValue = cal.get(calUnit);
			while (cal.get(calUnit) == initialCalValue) {
				cal.add(Calendar.DATE, -1);
			}
			long startTime = cal.getTimeInMillis();
			
			cal.add(Calendar.DATE, 1);
			while (cal.get(calUnit) == initialCalValue) {
				cal.add(Calendar.DATE, 1);
			}
			long endTime = cal.getTimeInMillis();
			
			return new TimeWindow(startTime, endTime);
		} else {
			long windowSize = unit.inMillis() * interval;
			long startTime = now - (now % windowSize);
			return new TimeWindow(startTime, startTime + windowSize);
		}
	}
	
	public long startOfDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		return cal.getTimeInMillis();
	}
	
	public long addDays(long time, int days) {
		return time + (MILLIS_IN_DAY * days);
	}
	
	private static final long MILLIS_IN_DAY = 1000L * 60L * 60L * 24L;
	
	
	private static int toCalendarField(TimeWindowUnit unit) {
		switch (unit) {
		case MINUTE:
			return Calendar.MINUTE;
			
		case HOUR:
			return Calendar.HOUR_OF_DAY;
			
		case DAY:
			return Calendar.DATE;
			
		case WEEK:
			return Calendar.WEEK_OF_YEAR;
			
		case MONTH:
			return Calendar.MONTH;
			
		default:
			throw new IllegalArgumentException(String.format("Time unit '%s' is not supported", unit));
		}
	}
	
	
	protected DateTimeUtils() { }
	
}
