package org.javarosa.core.model.data;

import org.javarosa.core.model.*;

public class Selection {
	public int index;
	public QuestionDef question; //cannot hold reference directly to selectItems, as it is wiped out and rebuilt after every locale change
	
	public Selection (int index, QuestionDef question) {
		this.index = index;
		this.question = question;
	}
	
	public String getText () {
		return (String)question.getSelectItems().keyAt(index);
	}
	
	public String getValue () {
		return (String)question.getSelectItems().elementAt(index);
	}
}
