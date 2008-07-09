package org.javarosa.formmanager.model.temp;

public class Selection {
	public int index;
	public Prompt question; //cannot hold reference directly to selectMap, as it is wiped out and rebuilt after every locale change
	
	public Selection (int index, Prompt question) {
		this.index = index;
		this.question = question;
	}
	
	public String getText () {
		return (String)question.getSelectMap().keyAt(index);
	}
	
	public String getValue () {
		return (String)question.getSelectMap().elementAt(index);
	}
}
