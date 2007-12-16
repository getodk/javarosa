package org.celllife.clforms.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.microedition.lcdui.ChoiceGroup;

import org.celllife.clforms.api.Constants;
import org.celllife.clforms.api.Prompt;

public class J2MEUtil {

	public J2MEUtil() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Converts the value object into a String based on the returnType
	 * 
	 * @return
	 */
	public static String getStringValue(Object val, int returnType) {
		String stringValue = "";
		if (val == null)
			return stringValue;

		switch (returnType) {
		case Constants.RETURN_DATE:
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

			stringValue = day + "/" + month + "/" + year;
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
				if(value.equalsIgnoreCase((String)prompt.getSelectMap().elementAt(i))){
					key = (String)prompt.getSelectMap().keyAt(i);
				}
			}
			
			System.out.println(value+"-Key-"+key);
			for(int i=0; i<collection.size(); i++){
				if(collection.getString(i).equalsIgnoreCase(key))
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
					if(value.equalsIgnoreCase((String)prompt.getSelectMap().elementAt(j))){
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
}
