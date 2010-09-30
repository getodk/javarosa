package org.javarosa.log.util;
import java.util.Date;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.Logger;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.log.properties.LogPropertyRules;

/**
 * 
 */

/**
 * @author ctsims
 *
 */
public class LogReportUtils {
	
	public static final int REPORT_FORMAT_SKIP = 0;
	public static final int REPORT_FORMAT_COMPACT = 1;
	public static final int REPORT_FORMAT_FULL = 2;
	
	/**
	 * Updates any enabled report types to set when their next
	 * report is pending. This method assumes that the most
	 * pertinent report was sent.
	 * 
	 * @param now
	 */
	public static void setPendingFromNow(long now, boolean daily, boolean weekly) {
		if (weekly) {
			setPendingWeekly(now);
		}
		if (daily) {
			setPendingDaily(now);
		}
	}
	
	private static void setPendingWeekly(long now) {
		Date nextWeekly = DateUtils.dateAdd(new Date(now), 7);
		PropertyManager._().setProperty(LogPropertyRules.LOG_NEXT_WEEKLY_SUBMIT, String.valueOf(nextWeekly.getTime()));
	}
	
	private static void setPendingDaily(long now) {
		Date nextDaily = DateUtils.dateAdd(new Date(now), 1);
		PropertyManager._().setProperty(LogPropertyRules.LOG_NEXT_DAILY_SUBMIT, String.valueOf(nextDaily.getTime()));
	}
	
	/**
	 * Note: This method is not guaranteed to set any values. If logging is 
	 * disabled, this will essentially perform a no-op. 
	 * 
	 * @param now
	 */
	public static void initPendingDates(long now) {
		//TODO: If the phone's clock is moved way back it's possible for these 
		//values to become stale. We should check for that somewhere.
		
		if(PropertyManager._().getSingularProperty(LogPropertyRules.LOG_NEXT_WEEKLY_SUBMIT) == null) {
			setPendingWeekly(now);
		}
		if(PropertyManager._().getSingularProperty(LogPropertyRules.LOG_NEXT_DAILY_SUBMIT) == null ){
			setPendingDaily(now);
		}
	}
	
	public static int getPendingWeeklyReportType(long now) {
		if(!JavaRosaPropertyRules.LOGS_ENABLED_YES.equals(PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED))) {
			return REPORT_FORMAT_SKIP;
		}
		
		return reportNeeded(now,PropertyManager._().getSingularProperty(LogPropertyRules.LOG_WEEKLY_SUBMIT), 
                PropertyManager._().getSingularProperty(LogPropertyRules.LOG_NEXT_WEEKLY_SUBMIT), 7);
	}
	
	public static int getPendingDailyReportType(long now) {
		if(!JavaRosaPropertyRules.LOGS_ENABLED_YES.equals(PropertyManager._().getSingularProperty(JavaRosaPropertyRules.LOGS_ENABLED))) {
			return REPORT_FORMAT_SKIP;
		}
		return reportNeeded(now,PropertyManager._().getSingularProperty(LogPropertyRules.LOG_DAILY_SUBMIT), 
                PropertyManager._().getSingularProperty(LogPropertyRules.LOG_NEXT_DAILY_SUBMIT), 1);
	}
	
	private static int reportNeeded(long now, String mode, String next, int period) {
		if(LogPropertyRules.SHORT.equals(mode) ||
		   LogPropertyRules.FULL.equals(mode)) {

			int maxNextDays = Math.max(2 * period, 3);
			
			long nextTime = getNextTimeForString(next);
			if (nextTime - now > maxNextDays * 86400000L) {
				Logger.log("device-report", "next send time is suspiciously far in future [" + nextTime + "]; forcing send");
				nextTime = now;
			}
			
			if(now >= nextTime) {
				if(LogPropertyRules.SHORT.equals(mode)) {
					return REPORT_FORMAT_COMPACT;
				} else {
					return REPORT_FORMAT_FULL;
				}
			}
		}
		return REPORT_FORMAT_SKIP;
	}
	
	
	private static long getNextTimeForString(String pending) {
		long nextTime;
		if(pending == null) {
			nextTime = 0;
		} else {
			try {
				nextTime = Long.parseLong(pending);
			} catch(NumberFormatException ife) {
				nextTime = 0;
			}
		}
		return nextTime;
	}
}
