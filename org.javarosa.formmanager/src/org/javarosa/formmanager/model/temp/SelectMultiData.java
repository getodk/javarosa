package org.javarosa.formmanager.model.temp;

import java.util.Vector;

public class SelectMultiData implements QuestionData {
	Vector vs; //vector of Selection
	
	public SelectMultiData (Vector vs) {
		setValue(vs);
	}
	
	public void setValue (Object o) {
		vs = (Vector)o;
		
		//validate type
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
		}
	}
	
	public Object getValue () {
		return vs;
	}
	
	public String getDisplayText () {
		String str = "";
		
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
			str += s.getText();
			if (i < vs.size() - 1)
				str += ", ";
		}		
		
		return str;
	}
}
