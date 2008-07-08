package org.javarosa.formmanager.model.temp;

public class SelectOneData implements QuestionData {
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
