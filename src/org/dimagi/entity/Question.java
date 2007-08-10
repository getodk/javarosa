package org.dimagi.entity;

/**
 * Question is a basic entity object equivilant to a single question
 * to be asked by the user. 
 * 
 * It contains a long and short version of the text
 * of the question, the type of widget that should be used to input the answer,
 * the answer to the question, and an array of objects that are used to contain
 * an arbitrary set of information. 
 * 
 * The internal Array can be used to hold widget-needed data, like different 
 * options for an answer, or data points for a graph.
 * 
 * The Answer is the only mutable portion of the question.
 *    
 * @author ctsims
 * @date Aug-07-2007
 *
 */
public class Question {
	private String _longText;
	private String _shortText;
	private int _widgetType;
	private String[] _internalArray;
	
	/**
	 * Creates a new question with the given data 
	 * @param longText The Long Version of the question to be displayed
	 * @param shortText A very short (2-3 word) string to label the question
	 * @param widgetType The type of widget to be used to answer this question
	 * @param internalArray The internal widget-specific data array
	 */
	public Question(String longText, String shortText, int widgetType, String[] internalArray) {
		_longText = longText;
		_shortText = shortText;
		_widgetType = widgetType;
		_internalArray = internalArray;
	}
	
	/**
	 * @return The Long Version of the question to be displayed
	 */
	public String getLongText() {
		return _longText;
	}
	
	/**
	 * @return A very short (2-3 word) string to label the question
	 */
	public String getShortText() {
		return _shortText;
	}
	
	/**
	 * @return widgetType The type of widget to be used to answer this question
	 */
	public int getWidgetType() {
		return _widgetType;
	}
	
	/**
	 * @return The internal widget-specific data array
	 */
	public String[] getInternalArray() {
		return _internalArray;
	}
}
