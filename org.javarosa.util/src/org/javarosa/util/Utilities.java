package org.javarosa.util;

import java.util.Calendar;
import java.util.Date;

public class Utilities {

	private Utilities(){
		
	}
	
	/** 
	 * Creates a new Globally Unique Identifier.
	 * 
	 * @return - the new guid.
	 */
	public static String getNewGuid(){
		return String.valueOf(new java.util.Date().getTime()); //this needs to be replaced with a realistic implementation
	}
	
	public static String DateToString(Date d){
		Calendar cd = Calendar.getInstance(java.util.TimeZone.getTimeZone("GMT+830"));
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);
		
		if (month.length()<2)
			month = "0" + month;
		
		if (day.length()<2)
			day = "0" + day;
		
		//return day + "-" + month + "-" + year;
		//TODO The date format should be flexibly set by the user.
		return year + "-" + month + "-" + day;
	}
	
	public static boolean stringToBoolean(String val){
		if(val == null)
			return false;
		return !val.equals("0");
	}
	
	public static String booleanToString(boolean val){
		if(val)
			return "1";
		return "0";
	}
}
