package org.javarosa.formmanager.model.temp;

public class StringData implements QuestionData {
	String s;
	
	public StringData (String s) {
		setValue(s);
	}
	
	public void setValue (Object o) {
		s = (String)o;
		if (s == null)
			s = "";
	}
	
	public Object getValue () {
		return s;
	}
	
	public String getDisplayText () {
		return s;
	}
}
