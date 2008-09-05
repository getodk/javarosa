package org.javarosa.core.model.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

import org.javarosa.core.model.data.IntegerData;

/**
 * Static utility methods for Dates in j2me 
 * 
 * @author Clayton Sims
 *
 */
public class DateUtils {

	public DateUtils() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * Converts the date into a string formatted according to the xsd:dateTime specifications
	 * @param date The date to be converted
	 * @return The date represented by 'date' in the XML dateTime format.
	 */
	//yyyy-mm-ddThh:mm:ss.sssZ or yyyy-mm-ddThh:mm:ss.sss(+-)hh:mm
	public static String formatDateToTimeStamp(Date date) {
		String dateElement = getXMLStringValue(date);
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		
		String hour = intPad(cd.get(Calendar.HOUR_OF_DAY), 2);
		String minute = intPad(cd.get(Calendar.MINUTE), 2);
		String second = intPad(cd.get(Calendar.SECOND), 2);
		String secfrac = intPad(cd.get(Calendar.MILLISECOND), 3);
		
		//want to add time zone info to be fully ISO-8601 compliant, but API is totally on crack!
		String time = hour + ":"+ minute + ":" + second + "." + secfrac; 
		return dateElement + "T" + time;
	}

	public static String intPad (int n, int pad) {
		String s = String.valueOf(n);
		while (s.length() < pad)
			s = "0" + s;
		return s;
	}
	
	/**
	 * Converts the value object into a String based on the returnType
	 * Note: This is for a short
	 *
	 * @return
	 */
	public static String getShortStringValue(Date val) {
		String stringValue = "";
		if (val == null) {
			return stringValue;
		}
		Date d = (Date) val;
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH) + 1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);

		if (month.length() < 2)
			month = "0" + month;

		if (day.length() < 2)
			day = "0" + day;

		stringValue = day + "/" + month + "/" + year.substring(2, 4);
		return stringValue;
	}
	
	/**
	 * Converts the value object into a String based on the returnType Note:
	 * This is for an xml formatted xsd:date datatype, the formatting must be
	 * YYYY-MM-DD
	 * 
	 * @return
	 */
	public static String getXMLStringValue(Date val) {
		String stringValue = "";
		if (val == null){
			return stringValue;
		}
		
		Date d = (Date) val;
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);

		if (month.length() < 2)
			month = "0" + month;

		if (day.length() < 2)
			day = "0" + day;

		stringValue =  year + "-" + month + "-" + day;
		return stringValue;
	}

	private static Vector tokenize(String values, char c) {
		Vector temp = new Vector();
		int pos = 0;
		int index = values.indexOf(c);
		while(index != -1){
			String tempp = values.substring(pos, index).trim();
			//System.out.println(tempp+pos+index);
			temp.addElement(tempp);
			pos = index+1;
			index = values.indexOf(c,pos);
		}
		temp.addElement(values.substring(pos).trim());
		return temp;
	}
	
	//returns a date object set to midnight on the given date in the current timezone *including DST!!*
	public static Date getDateFromString(String value) {
		Vector digits = tokenize(value, '-');

		if (digits.size() != 3)
			return null;
		
		int day, month, year;
		try {
			day = Integer.parseInt((String)digits.elementAt(2));
			month = Integer.parseInt((String)digits.elementAt(1));
			year = Integer.parseInt((String)digits.elementAt(0));
		} catch (NumberFormatException nfe) {
			return null;
		}
		
		return getDate(year, month, day);
	}
	
	public static Date getDate (int year, int month, int day) {
		month -= 1;
		
		if (month < Calendar.JANUARY || month > Calendar.DECEMBER || day < 1 || day > daysInMonth(month, year))
			return null;
		
		Calendar cd = Calendar.getInstance();
		cd.set(Calendar.DAY_OF_MONTH, day);
		cd.set(Calendar.MONTH, month);
		cd.set(Calendar.YEAR, year);
		cd.set(Calendar.HOUR_OF_DAY, 0);
		cd.set(Calendar.MINUTE, 0);
		cd.set(Calendar.SECOND, 0);
		cd.set(Calendar.MILLISECOND, 0);
		
		return cd.getTime();
	}
	
	//return new Date object with same date but time set to midnight (in current timezone)
	public static Date roundDate (Date d) {
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
		return DateUtils.getDate(cd.get(Calendar.YEAR), cd.get(Calendar.MONTH) + 1, cd.get(Calendar.DAY_OF_MONTH));
	}
	
	public static String get24HourTimeFromDate(Date d)
	{
		//set as the xml transport standard for time Questions
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
	
		String hour = "" + cd.get(Calendar.HOUR_OF_DAY);
		String minutes = "" + cd.get(Calendar.MINUTE);
		
			if (hour.length() <2)
				hour = "0" + hour;
			
			if (minutes.length() < 2)
				minutes = "0" + minutes;
			
			return hour+":"+minutes;	
	}
	
	public static int daysInMonth (int month, int year) {
		if (month == Calendar.APRIL || month == Calendar.JUNE || month == Calendar.SEPTEMBER || month == Calendar.NOVEMBER) {
			return 30;
		} else if (month == Calendar.FEBRUARY) {
			return 28 + (isLeap(year) ? 1 : 0);
		} else {
			return 31;
		}
	}
	
	public static boolean isLeap (int year) {
		return year % 4 == 0 && (year % 100 != 0 || year % 400 == 0);
	}
	
    public static String[] split(String original, String delimiter) {
        Vector nodes = new Vector();
        // Parse nodes into vector
        int index = original.indexOf(delimiter);
        while(index>=0) {
            nodes.addElement( original.substring(0, index) );
            original = original.substring(index+delimiter.length());
            index = original.indexOf(delimiter);
        }
        // Get the last node
        nodes.addElement( original );
        
        // Create splitted string array
        String[] result = new String[ nodes.size() ];
        if( nodes.size()>0 ) {
            for(int loop=0; loop<nodes.size(); loop++) {
                result[loop] = (String)nodes.elementAt(loop);
            }
            
        }
        return result;
    }	
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
			d = new Date(ref.getTime() - diff * 86400000l);
		} else if (type.equals("month")) {
			//not supported
		} else {
			throw new IllegalArgumentException();
		}
		
		return d;
	}
	
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
	 * @param earlierDate The earlier of the two dates to Diff
	 * @param laterDate The later of the two dates to Diff
	 * @return The approximate difference, in days, between the two dates given.
	 * The estimate is most likely to be a small underestimate.
	 */
	public static int getApproxDaysDifference(Date earlierDate, Date laterDate) {
		Date span = new Date(laterDate.getTime() - earlierDate.getTime());
		Date firstDate = new Date(0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(firstDate);
		int firstYear = calendar.get(Calendar.YEAR);
		int firstMonth = calendar.get(Calendar.MONTH);
		int firstDay = calendar.get(Calendar.DAY_OF_MONTH);
		
		calendar.setTime(span);
		int spanYear = calendar.get(Calendar.YEAR);
		int spanMonth = calendar.get(Calendar.MONTH);
		int spanDay = calendar.get(Calendar.DAY_OF_MONTH);
		
		int days = (spanYear - firstYear)*365 + (spanMonth - firstMonth)*30 + (spanDay - firstDay);
		
		return days;
	}
}
