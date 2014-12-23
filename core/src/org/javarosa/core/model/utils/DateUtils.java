/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;

import org.javarosa.core.services.locale.Localization;
import org.javarosa.core.util.MathUtils;

/**
 * Static utility methods for Dates in j2me
 *
 * @author Clayton Sims
 *
 */

public class DateUtils {
	private static final int MONTH_OFFSET = (1 - Calendar.JANUARY);

	public static final int FORMAT_ISO8601 = 1;
	public static final int FORMAT_HUMAN_READABLE_SHORT = 2;
	public static final int FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY = 5;
	//public static final int FORMAT_HUMAN_READABLE_LONG = 3;
	public static final int FORMAT_TIMESTAMP_SUFFIX = 7;

	/** RFC 822 **/
	public static final int FORMAT_TIMESTAMP_HTTP = 9;

	public static final long DAY_IN_MS = 86400000l;

	public DateUtils() {
		super();
	}

	public static class DateFields {
		public DateFields () {
			year = 1970;
			month = 1;
			day = 1;
			hour = 0;
			minute = 0;
			second = 0;
			secTicks = 0;
			dow = 0;

//			tzStr = "Z";
//			tzOffset = 0;
		}

		public int year;
		public int month; //1-12
		public int day; //1-31
		public int hour; //0-23
		public int minute; //0-59
		public int second; //0-59
		public int secTicks; //0-999 (ms)

		/** NOTE: CANNOT BE USED TO SPECIFY A DATE **/
		public int dow; //1-7;

//		public String tzStr;
//		public int tzOffset; //s ahead of UTC

		public boolean check () {
			return (inRange(month, 1, 12) && inRange(day, 1, daysInMonth(month - MONTH_OFFSET, year)) &&
					inRange(hour, 0, 23) && inRange(minute, 0, 59) && inRange(second, 0, 59) && inRange(secTicks, 0, 999));
		}
	}

	public static DateFields getFields (Date d) {
		return getFields(d, null);
	}

	public static DateFields getFields (Date d, String timezone) {
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
		if(timezone != null) {
			cd.setTimeZone(TimeZone.getTimeZone(timezone));
		}

		DateFields fields = new DateFields();
		fields.year = cd.get(Calendar.YEAR);
		fields.month = cd.get(Calendar.MONTH) + MONTH_OFFSET;
		fields.day = cd.get(Calendar.DAY_OF_MONTH);
		fields.hour = cd.get(Calendar.HOUR_OF_DAY);
		fields.minute = cd.get(Calendar.MINUTE);
		fields.second = cd.get(Calendar.SECOND);
		fields.secTicks = cd.get(Calendar.MILLISECOND);
		fields.dow = cd.get(Calendar.DAY_OF_WEEK);

		return fields;
	}

	public static Date getDate (DateFields f) {
		return getDate(f, null);
	}

	public static Date getDate (DateFields f, String timezone) {
		Calendar cd = Calendar.getInstance();
		if(timezone != null) {
			cd.setTimeZone(TimeZone.getTimeZone(timezone));
		}
		cd.set(Calendar.YEAR, f.year);
		cd.set(Calendar.MONTH, f.month - MONTH_OFFSET);
		cd.set(Calendar.DAY_OF_MONTH, f.day);
		cd.set(Calendar.HOUR_OF_DAY, f.hour);
		cd.set(Calendar.MINUTE, f.minute);
		cd.set(Calendar.SECOND, f.second);
		cd.set(Calendar.MILLISECOND, f.secTicks);

		return cd.getTime();
	}

	/* ==== FORMATTING DATES/TIMES TO STANDARD STRINGS ==== */

	public static String formatDateTime (Date d, int format) {
		if (d == null) {
			return "";
		}

		DateFields fields = getFields(d, format == FORMAT_TIMESTAMP_HTTP ? "UTC" : null);

		String delim;
		switch (format) {
		case FORMAT_ISO8601: delim = "T"; break;
		case FORMAT_TIMESTAMP_SUFFIX: delim = ""; break;
		case FORMAT_TIMESTAMP_HTTP: delim = " "; break;
		default: delim = " "; break;
		}

		return formatDate(fields, format) + delim + formatTime(fields, format);
	}

	public static String formatDate (Date d, int format) {
		return (d == null ? "" :formatDate(getFields(d, format == FORMAT_TIMESTAMP_HTTP ? "UTC" : null), format));
	}

	public static String formatTime (Date d, int format) {
		return (d == null ? "" : formatTime(getFields(d, format == FORMAT_TIMESTAMP_HTTP ? "UTC" : null), format));
	}

	private static String formatDate (DateFields f, int format) {
		switch (format) {
		case FORMAT_ISO8601: return formatDateISO8601(f);
		case FORMAT_HUMAN_READABLE_SHORT: return formatDateColloquial(f);
		case FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY: return formatDaysFromToday(f);
		case FORMAT_TIMESTAMP_SUFFIX: return formatDateSuffix(f);
		case FORMAT_TIMESTAMP_HTTP: return formatDateHttp(f);
		default: return null;
		}
	}

	private static String formatTime (DateFields f, int format) {
		switch (format) {
		case FORMAT_ISO8601: return formatTimeISO8601(f);
		case FORMAT_HUMAN_READABLE_SHORT: return formatTimeColloquial(f);
		case FORMAT_TIMESTAMP_SUFFIX: return formatTimeSuffix(f);
		case FORMAT_TIMESTAMP_HTTP: return formatTimeHttp(f);
		default: return null;
		}
	}

	/** RFC 822 **/
	private static String formatDateHttp(DateFields f) {
		return format(f, "%a, %d %b %Y");
	}

	/** RFC 822 **/
	private static String formatTimeHttp(DateFields f) {
		return format(f, "%H:%M:%S GMT");
	}

	private static String formatDateISO8601 (DateFields f) {
		return f.year + "-" + intPad(f.month, 2) + "-" + intPad(f.day, 2);
	}

	private static String formatDateColloquial (DateFields f) {
		String year = Integer.valueOf(f.year).toString();

		//Normal Date
		if(year.length() == 4) {
			year = year.substring(2, 4);
		}
		//Otherwise we have an old or bizzarre date, don't try to do anything

		return intPad(f.day, 2) + "/" + intPad(f.month, 2) + "/" + year;
	}

	private static String formatDateSuffix (DateFields f) {
		return f.year + intPad(f.month, 2) + intPad(f.day, 2);
	}

	private static String formatTimeISO8601 (DateFields f) {
		String time = intPad(f.hour, 2) + ":" + intPad(f.minute, 2) + ":" + intPad(f.second, 2) + "." + intPad(f.secTicks, 3);

		//Time Zone ops (1 in the first field corresponds to 'CE' ERA)
		int offset = TimeZone.getDefault().getOffset(1,f.year, f.month - 1, f.day, f.dow, 0);

		//NOTE: offset is in millis
		if(offset ==0 ) {
			time += "Z";
		}
		else {

			//Start with sign
			String offsetSign = offset >0 ? "+" : "-";

			int value = Math.abs(offset) / 1000 / 60;

			String hrs = intPad(value / 60, 2);
			String mins = value % 60 != 0 ? ":" + intPad(value % 60, 2) :"";

			time += offsetSign + hrs + mins;
		}
		return time;
	}

	private static String formatTimeColloquial (DateFields f) {
		return intPad(f.hour, 2) + ":" + intPad(f.minute, 2);
	}

	private static String formatTimeSuffix (DateFields f) {
		return intPad(f.hour, 2) + intPad(f.minute, 2) + intPad(f.second, 2);
	}

	public static String format (Date d, String format) {
		return format(getFields(d), format);
	}

	public static String format (DateFields f, String format) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < format.length(); i++) {
			char c = format.charAt(i);

			if (c == '%') {
				i++;
				if (i >= format.length()) {
					throw new RuntimeException("date format string ends with %");
				} else {
					c = format.charAt(i);
				}

				if (c == '%') {			//literal '%'
					sb.append("%");
				} else if (c == 'Y') {	//4-digit year
					sb.append(intPad(f.year, 4));
				} else if (c == 'y') {	//2-digit year
					sb.append(intPad(f.year, 4).substring(2));
				} else if (c == 'm') {	//0-padded month
					sb.append(intPad(f.month, 2));
				} else if (c == 'n') {	//numeric month
					sb.append(f.month);
				} else if (c == 'b') {	//short text month
					String[] months = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
					sb.append(months[f.month - 1]);
				} else if (c == 'd') {	//0-padded day of month
					sb.append(intPad(f.day, 2));
				} else if (c == 'e') {	//day of month
					sb.append(f.day);
				} else if (c == 'H') {	//0-padded hour (24-hr time)
					sb.append(intPad(f.hour, 2));
				} else if (c == 'h') {	//hour (24-hr time)
					sb.append(f.hour);
				} else if (c == 'M') {	//0-padded minute
					sb.append(intPad(f.minute, 2));
				} else if (c == 'S') {	//0-padded second
					sb.append(intPad(f.second, 2));
				} else if (c == '3') {	//0-padded millisecond ticks (000-999)
					sb.append(intPad(f.secTicks, 3));
				} else if (c == 'a') {	//Three letter short text day
					String[] dayNames = new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
					sb.append(dayNames[f.dow - 1]);
				} else if (c == 'Z' || c == 'A' || c == 'B') {
					throw new RuntimeException("unsupported escape in date format string [%" + c + "]");
				} else {
					throw new RuntimeException("unrecognized escape in date format string [%" + c + "]");
				}
			} else {
				sb.append(c);
			}
		}

		return sb.toString();
	}

	/* ==== PARSING DATES/TIMES FROM STANDARD STRINGS ==== */

	public static Date parseDateTime (String str) {
		DateFields fields = new DateFields();
		int i = str.indexOf("T");
		if (i != -1) {
			if (!parseDate(str.substring(0, i), fields) || !parseTime(str.substring(i + 1), fields)) {
				return null;
			}
		} else {
			if (!parseDate(str, fields)) {
				return null;
			}
		}
		return getDate(fields);
	}

	public static Date parseDate (String str) {
		DateFields fields = new DateFields();
		if (!parseDate(str, fields)) {
			return null;
		}
		return getDate(fields);
	}

	public static Date parseTime (String str) {
		DateFields fields = new DateFields();
		if (!parseTime(str, fields)) {
			return null;
		}
		return getDate(fields);
	}

	private static boolean parseDate (String dateStr, DateFields f) {
      List<String> pieces = split(dateStr, "-", false);
		if (pieces.size() != 3)
			return false;

		try {
			f.year = Integer.parseInt((String)pieces.get(0));
			f.month = Integer.parseInt((String)pieces.get(1));
			f.day = Integer.parseInt((String)pieces.get(2));
		} catch (NumberFormatException nfe) {
			return false;
		}

		return f.check();
	}

	private static boolean parseTime (String timeStr, DateFields f) {
		//get timezone information first. Make a Datefields set for the possible offset
		//NOTE: DO NOT DO DIRECT COMPUTATIONS AGAINST THIS. It's a holder for hour/minute
		//data only, but has data in other fields
		DateFields timeOffset = null;

		if(timeStr.charAt(timeStr.length() -1) == 'Z') {
			//UTC!

			//Clean up string for later processing
			timeStr = timeStr.substring(0, timeStr.length() -1);
			timeOffset = new DateFields();
		} else if(timeStr.indexOf("+") != -1 || timeStr.indexOf("-") != -1) {
			timeOffset = new DateFields();

         List<String> pieces = split(timeStr, "+", false);

			//We're going to add the Offset straight up to get UTC
			//so we need to invert the sign on the offset string
			int offsetSign = -1;

			if(pieces.size() > 1) {
				//offsetSign is already correct
			} else {
				pieces = split(timeStr, "-", false);
				offsetSign = 1;
			}

			timeStr = pieces.get(0);

			String offset = pieces.get(1);
			String hours = offset;
			if(offset.indexOf(":") != -1) {
            List<String> tzPieces = split(offset, ":", false);
				hours = tzPieces.get(0);
				int mins = Integer.parseInt(tzPieces.get(1));
				timeOffset.minute = mins * offsetSign;
			}
			timeOffset.hour = Integer.parseInt(hours) * offsetSign;
		}

		//Do the actual parse for the real time values;
		if(!parseRawTime(timeStr, f)) {
			return false;
		}

		if(!(f.check())) {
			return false;
		}

		//Time is good, if there was no timezone info, just return that;
		if(timeOffset == null) {
			return true;
		}

		//Now apply any relevant offsets from the timezone.
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		c.setTime(new Date(DateUtils.getDate(f, "UTC").getTime() + (((60 * timeOffset.hour)  + timeOffset.minute) * 60 * 1000)));

		//c is now in the timezone of the parsed value, so put
		//it in the local timezone.

		c.setTimeZone(TimeZone.getDefault());
		long four = c.get(Calendar.HOUR);

		DateFields adjusted = getFields(c.getTime());

		f.hour = adjusted.hour;
		f.minute = adjusted.minute;
		f.second = adjusted.second;
		f.secTicks = adjusted.secTicks;

		return f.check();
	}

	/**
	 * Parse the raw components of time (hh:mm:ss) with no timezone information
	 *
	 * @param timeStr
	 * @param f
	 * @return
	 */
	private static boolean parseRawTime (String timeStr, DateFields f) {
      List<String> pieces = split(timeStr, ":", false);
		if (pieces.size() != 2 && pieces.size() != 3)
			return false;

		try {
			f.hour = Integer.parseInt((String)pieces.get(0));
			f.minute = Integer.parseInt((String)pieces.get(1));

			if (pieces.size() == 3) {
				String secStr = (String)pieces.get(2);
				int i;
				for (i = 0; i < secStr.length(); i++) {
					char c = secStr.charAt(i);
					if (!Character.isDigit(c) && c != '.')
						break;
				}
				secStr = secStr.substring(0, i);

				double fsec = Double.parseDouble(secStr);
				f.second = (int)fsec;
				f.secTicks = (int)(1000.0 * (fsec - f.second));
			}
		} catch (NumberFormatException nfe) {
			return false;
		}

		return f.check();
	}


	/* ==== DATE UTILITY FUNCTIONS ==== */

	public static Date getDate (int year, int month, int day) {
		DateFields f = new DateFields();
		f.year = year;
		f.month = month;
		f.day = day;
		return (f.check() ? getDate(f) : null);
	}

	/**
	 *
	 * @return new Date object with same date but time set to midnight (in current timezone)
	 */
	public static Date roundDate (Date d) {
		if ( d == null ) return null;
		DateFields f = getFields(d);
		return getDate(f.year, f.month, f.day);
	}

	public static Date today () {
		return roundDate(new Date());
	}

	/* ==== CALENDAR FUNCTIONS ==== */

	/**
	 * Returns the fractional time within the local day.
	 *
	 * @param d
	 * @return
	 */
	public static double decimalTimeOfLocalDay(Date d) {
		long milli = d.getTime();
		// time is local time.
		// We want to obtain milliseconds from start of local day.
		// the Math.floor() function below will do milliseconds from
		// start of UTC day. Adjust back to UTC time-of-day.
		Calendar c = Calendar.getInstance(TimeZone.getDefault());
		long milliOff = (c.get(Calendar.ZONE_OFFSET) + c.get(Calendar.DST_OFFSET));
		milli += milliOff;
		// and now convert to fractional day.
		Double v = ((double) milli) / DAY_IN_MS;
		return v - Math.floor(v);
	}

	/**
	 * Returns the number of days in the month given for
	 * a given year.
	 * @param month The month to be tested
	 * @param year The year in which the month is to be tested
	 * @return the number of days in the given month on the given
	 * year.
	 */
	public static int daysInMonth (int month, int year) {
		if (month == Calendar.APRIL || month == Calendar.JUNE || month == Calendar.SEPTEMBER || month == Calendar.NOVEMBER) {
			return 30;
		} else if (month == Calendar.FEBRUARY) {
			return 28 + (isLeap(year) ? 1 : 0);
		} else {
			return 31;
		}
	}

	/**
	 * Determines whether a year is a leap year in the
	 * proleptic Gregorian calendar.
	 *
	 * @param year The year to be tested
	 * @return True, if the year given is a leap year,
	 * false otherwise.
	 */
	public static boolean isLeap (int year) {
		return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
	}


	/* ==== Parsing to Human Text ==== */

	/**
	 * Provides text representing a span of time.
	 *
	 * @param f The fields for the date to be compared against the current date.
	 * @return a string which is a human readable representation of the difference between
	 * the provided date and the current date.
	 */
	private static String formatDaysFromToday(DateFields f) {
		String daysAgoStr = "";
		Date d = DateUtils.getDate(f);
		int daysAgo = DateUtils.daysSinceEpoch(new Date()) - DateUtils.daysSinceEpoch(d);

		if (daysAgo == 0) {
			return Localization.get("date.today");
		} else if (daysAgo == 1) {
			return Localization.get("date.yesterday");
		} else if (daysAgo == 2) {
			return Localization.get("date.twoago", new String[] {String.valueOf(daysAgo)});
		} else if (daysAgo > 2 && daysAgo <= 6) {
			return Localization.get("date.nago", new String[] {String.valueOf(daysAgo)});
		} else if (daysAgo == -1) {
			return Localization.get("date.tomorrow");
		} else if (daysAgo < -1 && daysAgo >= -6) {
			return Localization.get("date.nfromnow", new String[] {String.valueOf(-daysAgo)});
		} else {
			return DateUtils.formatDate(f, DateUtils.FORMAT_HUMAN_READABLE_SHORT);
		}
	}

	/* ==== DATE OPERATIONS ==== */

    /**
     * Creates a Date object representing the amount of time between the
     * reference date, and the given parameters.
     * @param ref The starting reference date
     * @param type "week", or "month", representing the time period which is to be returned.
     * @param start "sun", "mon", ... etc. representing the start of the time period.
     * @param beginning true=return first day of period, false=return last day of period
     * @param includeToday Whether to include the current date in the returned calculation
     * @param nAgo How many periods ago. 1=most recent period, 0=period in progress
     * @return a Date object representing the amount of time between the
     * reference date, and the given parameters.
     */
	public static Date getPastPeriodDate (Date ref, String type, String start, boolean beginning, boolean includeToday, int nAgo) {
		Date d = null;

		if (type.equals("week")) {
			//1 week period
			//start: day of week that starts period
			//beginning: true=return first day of period, false=return last day of period
			//includeToday: whether today's date can count as the last day of the period
			//nAgo: how many periods ago; 1=most recent period, 0=period in progress

			int target_dow = -1, current_dow = -1, diff;
			int offset = (includeToday ? 1 : 0);

			if (start.equals("sun")) target_dow = 0;
			else if (start.equals("mon")) target_dow = 1;
			else if (start.equals("tue")) target_dow = 2;
			else if (start.equals("wed")) target_dow = 3;
			else if (start.equals("thu")) target_dow = 4;
			else if (start.equals("fri")) target_dow = 5;
			else if (start.equals("sat")) target_dow = 6;

			if (target_dow == -1)
				throw new RuntimeException();

			Calendar cd = Calendar.getInstance();
			cd.setTime(ref);

			switch(cd.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SUNDAY: current_dow = 0; break;
			case Calendar.MONDAY: current_dow = 1; break;
			case Calendar.TUESDAY: current_dow = 2; break;
			case Calendar.WEDNESDAY: current_dow = 3; break;
			case Calendar.THURSDAY: current_dow = 4; break;
			case Calendar.FRIDAY: current_dow = 5; break;
			case Calendar.SATURDAY: current_dow = 6; break;
			default: throw new RuntimeException(); //something is wrong
			}

			diff = (((current_dow - target_dow) + (7 + offset)) % 7 - offset) + (7 * nAgo) - (beginning ? 0 : 6); //booyah
			d = new Date(ref.getTime() - diff * DAY_IN_MS);
		} else if (type.equals("month")) {
			//not supported
		} else {
			throw new IllegalArgumentException();
		}

		return d;
	}

	/**
	 * Gets the number of months separating the two dates.
	 * @param earlierDate The earlier date, chronologically
	 * @param laterDate The later date, chronologically
	 * @return the number of months separating the two dates.
	 */
	public static int getMonthsDifference(Date earlierDate, Date laterDate) {
		Date span = new Date(laterDate.getTime() - earlierDate.getTime());
		Date firstDate = new Date(0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstDate);
		int firstYear = calendar.get(Calendar.YEAR);
		int firstMonth = calendar.get(Calendar.MONTH);

		calendar.setTime(span);
		int spanYear = calendar.get(Calendar.YEAR);
		int spanMonth = calendar.get(Calendar.MONTH);
		int months = (spanYear - firstYear)*12 + (spanMonth - firstMonth);
		return months;
	}

	/**
	 * @param date the date object to be analyzed
	 * @return The number of days (as a double precision floating point) since the Epoch
	 */
	public static int daysSinceEpoch(Date date) {
		return dateDiff(getDate(1970, 1, 1), date);
	}


    public static Double fractionalDaysSinceEpoch(Date a) {
        return new Double((a.getTime() - getDate(1970, 1, 1).getTime()) / (double)DAY_IN_MS);
    }

	/**
	 * add n days to date d
	 *
	 * @param d
	 * @param n
	 * @return
	 */
	public static Date dateAdd (Date d, int n) {
		return roundDate(new Date(roundDate(d).getTime() + DAY_IN_MS * n + DAY_IN_MS / 2));
		//half-day offset is needed to handle differing DST offsets!
	}

	/**
	 * return the number of days between a and b, positive if b is later than a
	 *
	 * @param a
	 * @param b
	 * @return # days difference
	 */
	public static int dateDiff (Date a, Date b) {
		return (int)MathUtils.divLongNotSuck(roundDate(b).getTime() - roundDate(a).getTime() + DAY_IN_MS / 2, DAY_IN_MS);
		//half-day offset is needed to handle differing DST offsets!
	}

	/* ==== UTILITY ==== */

	/**
	 * Tokenizes a string based on the given delimiter string
	 * @param str The string to be split
	 * @param delimiter The delimeter to be used
	 * @return An array of strings contained in original which were
	 * seperated by the delimeter
	 */
    public static List<String> split (String str, String delimiter, boolean combineMultipleDelimiters) {

    	int index = str.indexOf(delimiter);
    	List<String> pieces = new ArrayList<String>(index+1);
        while (index >= 0) {
            pieces.add(str.substring(0, index));
            str = str.substring(index + delimiter.length());
            index = str.indexOf(delimiter);
        }
        pieces.add(str);

        if (combineMultipleDelimiters) {
        	for (int i = 0; i < pieces.size(); i++) {
        		if (((String)pieces.get(i)).length() == 0) {
        			pieces.remove(i);
        			i--;
        		}
        	}
        }

        return pieces;
    }

	/**
	 * Converts an integer to a string, ensuring that the string
	 * contains a certain number of digits
	 * @param n The integer to be converted
	 * @param pad The length of the string to be returned
	 * @return A string representing n, which has pad - #digits(n)
	 * 0's preceding the number.
	 */
	public static String intPad (int n, int pad) {
		String s = String.valueOf(n);
		while (s.length() < pad)
			s = "0" + s;
		return s;
	}

	private static boolean inRange(int x, int min, int max) {
		return (x >= min && x <= max);
	}

	/* ==== GARBAGE (backward compatibility; too lazy to remove them now) ==== */

	public static String formatDateToTimeStamp(Date date) {
		return formatDateTime(date, FORMAT_ISO8601);
	}

	public static String getShortStringValue(Date val) {
		return formatDate(val, FORMAT_HUMAN_READABLE_SHORT);
	}

	public static String getXMLStringValue(Date val) {
		return formatDate(val, FORMAT_ISO8601);
	}

	public static String get24HourTimeFromDate(Date d) {
		return formatTime(d, FORMAT_HUMAN_READABLE_SHORT);
	}

	public static Date getDateFromString(String value) {
		return parseDate(value);
	}

	public static Date getDateTimeFromString(String value) {
		return parseDateTime(value);
	}

	public static boolean stringContains(String string,String substring){
		if(string == null || substring == null){
			return false;
		}
		if(string.indexOf(substring)== -1){
			return false;
		}else{
			return true;
		}
	}

}
