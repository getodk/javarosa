package org.javarosa.core.model.data;

public class StringData implements AnswerData {
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
