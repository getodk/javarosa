package org.dimagi.entity;

import java.util.Vector;

public class Question {
	private String _longText;
	private String _shortText;
	private int _widgetType;
	private String[] _internalArray;
	
	public Question(String longText, String shortText, int widgetType) {
		_longText = longText;
		_shortText = shortText;
		_widgetType = widgetType;
	}
	
	public String getLongText() {
		return _longText;
	}
	
	public String getShortText() {
		return _shortText;
	}
	
	public int getWidgetType() {
		return _widgetType;
	}
	
	public void setInternalArray(String[] internalArray) {
		_internalArray = internalArray;
	}
	
	public String[] getInternalArray() {
		return _internalArray;
	}
}
