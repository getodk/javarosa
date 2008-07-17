package org.javarosa.core.model.data;

public class SelectOneData implements AnswerData {
	Selection s;
	
	public SelectOneData (Selection s) {
		setValue(s);
	}
	
	public void setValue (Object o) {
		s = (Selection)o;
	}
	
	public Object getValue () {
		return s;
	}
	
	public String getDisplayText () {
		return s.getText();
	}
}
