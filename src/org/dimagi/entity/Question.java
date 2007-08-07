package org.dimagi.entity;

public class Question {
	private String _longText;
	private String _shortText;
	
	public Question(String longText, String shortText) {
		_longText = longText;
		_shortText = shortText;
	}
	
	public String getLongText() {
		return _longText;
	}
	
	public String getShortText() {
		return _shortText;
	}
}
