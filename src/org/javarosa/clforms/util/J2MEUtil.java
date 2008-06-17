package org.javarosa.clforms.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.api.Prompt;

import de.enough.polish.util.TextUtil;

public class J2MEUtil {

	public J2MEUtil() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * turns a date into a ddd
	 *
	 * @return string
	 */
	public static String readableDateFormat(Date date) {

		String stringValue = "";
		/*if (date == null){
			//LOG
			System.out.println("string value null");
			return stringValue;
		}

		Calendar cd = Calendar.getInstance();
		cd.setTime(date);

		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);*/
		return stringValue;
	}


	/**
	 * Converts the date into a string formatted according to the xsd:dateTime specifications
	 * @param date The date to be converted
	 * @return The date represented by 'date' in the XML dateTime format.
	 */
	public static String formatDateToTimeStamp(Date date) {
		String dateElement = getXMLStringValue(date, Constants.RETURN_DATE);
		
		Calendar cd = Calendar.getInstance();
		cd.setTime(date);
		String hour = String.valueOf(cd.get(Calendar.HOUR));
		hour = hour.length() < 2 ? "0" + hour : hour;
		String minute = String.valueOf(cd.get(Calendar.MINUTE));
		minute = minute.length() < 2 ? "0" + minute : minute;
		String time = hour + ":"+ minute + ":" + String.valueOf(cd.get(Calendar.MILLISECOND)).substring(0,2);
		
		return dateElement + "T" + time;
	}

	/**
	 * Converts the value object into a String based on the returnType
	 * Note: This is for a short
	 *
	 * @return
	 */
	public static String getShortStringValue(Object val, int returnType) {
		String stringValue = "";
		if (val == null){
			//LOG
			System.out.println("string value null");
			return stringValue;
		}

		switch (returnType) {
		case Constants.RETURN_DATE:
			if(val instanceof Date){
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

				stringValue = day + "/" + month + "/" + year.substring(2,4);
			}else
				stringValue = val.toString();
				break;
		default:
			stringValue = val.toString();
		}
		return stringValue;
	}
	
	/**
	 * Converts the value object into a String based on the returnType
	 * Note: This is for an xml formatted xsd:date datatype, the 
	 * formatting must be YYYY-MM-DD
	 *
	 * @return
	 */
	public static String getXMLStringValue(Object val, int returnType) {
		String stringValue = "";
		if (val == null){
			//LOG
			System.out.println("string value null");
			return stringValue;
		}

		switch (returnType) {
		case Constants.RETURN_DATE:
			if(val instanceof Date){
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
			}else
				stringValue = val.toString();
				break;
		default:
			stringValue = val.toString();
		}
		return stringValue;
	}


	public static Object setSelected(Prompt prompt, ChoiceGroup collection) {

		switch (prompt.getReturnType()) {
		case Constants.RETURN_SELECT1:
			String key = null;
			String value = (String)prompt.getValue();
			for(int i=0; i<prompt.getSelectMap().size();i++){
				if(TextUtil.equalsIgnoreCase(value,(String)prompt.getSelectMap().elementAt(i))){
					key = (String)prompt.getSelectMap().keyAt(i);
				}
			}

			System.out.println(value+"-Key-"+key);
			for(int i=0; i<collection.size(); i++){
				if(TextUtil.equalsIgnoreCase(collection.getString(i),key))
					collection.setSelectedIndex(i,true);
			}
			break;
		case Constants.RETURN_SELECT_MULTI:
			Vector valuesVector = new Vector();
			String values = (String)prompt.getValue();
			// remove any []
			values = values.substring(values.indexOf('[')+1, values.indexOf(']'));
			System.out.println("VAL:"+values);
			// tokenize to vector
			valuesVector = tokenize(values,',');
			// 2D search through values
			// TODO can we improve this efficiency?
			Vector keys = new Vector();
			for (int i = 0; i < valuesVector.size(); i++) {
				System.out.println((String)valuesVector.elementAt(i));
				value = (String)valuesVector.elementAt(i);
				for(int j=0; j<prompt.getSelectMap().size();j++){
					if(TextUtil.equalsIgnoreCase(value,(String)prompt.getSelectMap().elementAt(j))){
						keys.addElement((String)prompt.getSelectMap().keyAt(j));
					}
				}

			}

			for(int i=0; i<collection.size(); i++){
				if(keys.contains(collection.getString(i)))
					collection.setSelectedIndex(i,true);
			}
			break;
		}
		return collection ;
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

	public static boolean getBoolean(String attributeValue) throws Exception {
		if (TextUtil.equalsIgnoreCase(attributeValue,"true()"))
			return true;
		else if (TextUtil.equalsIgnoreCase(attributeValue,"false()"))
			return false;
		else
			//TODO throw parse exception
			return false;
	}

	public static Date getDateFromString(String value) {
		Date result = new Date();
		Vector digits = tokenize(value, '-');

		int day = Integer.valueOf((String)digits.elementAt(2)).intValue();
		int month = Integer.valueOf((String)digits.elementAt(1)).intValue();
		month--;
		int year = Integer.valueOf((String)digits.elementAt(0)).intValue();

		Calendar cd = Calendar.getInstance();
		cd.set(Calendar.DAY_OF_MONTH, day);
		cd.set(Calendar.MONTH, month);
		cd.set(Calendar.YEAR, year);

		result = cd.getTime();

		return result;
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
}
