package network.piranya.platform.api.lang;

import java.util.Calendar;

public class Period {
	
	private final long startTime;
	public long startTime() { return startTime; }
	
	private final long endTime;
	public long endTime() { return endTime; }
	
	private Period(long startTime, long endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}
	
	public static Period between(long startTime, long endTime) {
		return new Period(startTime, endTime);
	}
	
	public static Period backAndUntilNow(long backInterval, TimeWindowUnit unit) {
		return new Period(getFixedStartTime(backInterval, unit), System.currentTimeMillis());
	}
	
	
	protected static long getFixedStartTime(long backInterval, TimeWindowUnit unit) {
		Calendar cal = Calendar.getInstance();
		if (unit == TimeWindowUnit.DAY) {
			if (backInterval > 1L) {
				cal.add(toCalendarField(unit), -(int)backInterval);
			}
			cal.set(Calendar.MILLISECOND, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.HOUR_OF_DAY, 0);
		} else {
			cal.add(toCalendarField(unit), -(int)backInterval);
		}
		return cal.getTimeInMillis();
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
	
}
