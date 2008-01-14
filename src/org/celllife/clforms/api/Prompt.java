package org.celllife.clforms.api;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.List;

import org.celllife.clforms.util.J2MEUtil;
import org.celllife.clforms.util.SimpleOrderedHashtable;
//import de.enough.polish.util.HashMap;


public class Prompt {

	private String id;
	private int formControlType;
	private int returnType;
	private int widgetType;
	private String longText;
	private String shortText;
	private String header;
	private boolean relevant;
	private boolean required;
	private Object defaultValue;
	private Object value;
	private int selectedIndex = -1;
	private String hint;
	private SimpleOrderedHashtable selectMap;
	private String xpathBinding;
	private String relevantString;
	private String bindID;
	private int labelPosition;
	
	public Prompt() {
		super();
		relevantString = null;
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getFormControlType() {
		return formControlType;
	}

	public void setFormControlType(int formControlType) {
		this.formControlType = formControlType;
	}

	public int getReturnType() {
		return returnType;
	}

	public void setReturnType(int returnType) {
		this.returnType = returnType;
	}

	public int getWidgetType() {
		return widgetType;
	}
	
	public void setWidgetType(int widgetType) {
		this.widgetType = widgetType;
	}
	
	public String getLongText() {
		return longText;
	}

	public void setLongText(String longText) {
		this.longText = longText;
	}

	public String getShortText() {
		return shortText;
	}

	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public boolean isRelevant() {
		return relevant;
	}

	public void setRelevant(boolean relevant) {
		this.relevant = relevant;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
			this.value = value;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public String getHint() {
		return hint;
	}

	public void setHint(String hint) {
		this.hint = hint;
	}

	public String toScript() {

		return value.toString();
	}

	public void setFromScript(String value) {
		// TODO set value from string (based on return type)
	}

	/**
	 * Converts the value object into a String based on the returnType
	 * 
	 * @return
	 */
	/*public String getStringValue(Object val) {
		String stringValue = "";
		if (val == null)
			return stringValue;

		switch (returnType) {
		case Constants.RETURN_DATE:
			Date d = (Date) val;
			Calendar cd = Calendar.getInstance();
			cd.setTime(d);
			String year = "" + cd.get(Calendar.YEAR);
			String month = "" + cd.get(Calendar.MONTH);
			String day = "" + cd.get(Calendar.DAY_OF_MONTH);

			if (month.length() < 2)
				month = "0" + month;

			if (day.length() < 2)
				day = "0" + day;

			stringValue = year + "-" + month + "-" + day;
			break;
		default:
			stringValue = val.toString();
		}
		return stringValue;
	}*/

	public SimpleOrderedHashtable getSelectMap() {
		return selectMap;
	}

	public void setSelectMap(SimpleOrderedHashtable selectMap) {
		this.selectMap = selectMap;
	}

	public String getXpathBinding() {
		return xpathBinding;
	}
	
	public void loadDefaultValue() {
		this.setValue(this.getDefaultValue());
	}

	public void setXpathBinding(String xpathBinding) {
		this.xpathBinding = xpathBinding;
	}

	public String getRelevantString() {
		return relevantString;
	}

	public void setRelevantString(String relevantString) {
		this.relevantString = relevantString;
	}

	public void setBindID(String bind) {
		// TODO just have pointer to bind object?? surely!
		this.bindID = bind;		
	}
	
	public String getBindID() {
		return bindID;
	}

	public int getLabelPosition() {
		return labelPosition;
	}
	
	public void setLabelPosition(int labelPosition) {
		this.labelPosition = labelPosition;
	}
	
}
