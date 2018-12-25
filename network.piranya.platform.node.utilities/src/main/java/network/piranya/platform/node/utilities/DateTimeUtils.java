package network.piranya.platform.node.utilities;

import java.util.Calendar;

import network.piranya.platform.api.lang.TimeWindow;
import network.piranya.platform.api.lang.TimeWindowUnit;

public abstract class DateTimeUtils {
	
	public static TimeWindow calcWindowStartAndEndTime(long now, TimeWindowUnit unit, long interval) {
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
	
	protected static int toCalendarField(TimeWindowUnit unit) {
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
	
	
	private DateTimeUtils() { }
	
}
