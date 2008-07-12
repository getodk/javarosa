package org.javarosa.formmanager.model.temp;

import java.util.Calendar;
import java.util.Date;

public class DateData implements QuestionData {
	Date d;
	
	public DateData (Date d) {
		setValue(d);
	}
	
	public void setValue (Object o) {
		d = (Date)o;
	}
	
	public Object getValue () {
		return d;
	}
	
	public String getDisplayText () {
		Calendar cd = Calendar.getInstance();
		cd.setTime(d);
		String year = "" + cd.get(Calendar.YEAR);
		String month = "" + (cd.get(Calendar.MONTH)+1);
		String day = "" + cd.get(Calendar.DAY_OF_MONTH);

		if (month.length() < 2)
			month = "0" + month;

		if (day.length() < 2)
			day = "0" + day;

		return day + "/" + month + "/" + year.substring(2,4);
	}
}
