package org.javarosa.clforms.api;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.List;

import org.javarosa.clforms.util.J2MEUtil;
import org.javarosa.clforms.util.SimpleOrderedHashtable;


public class Prompt {

	private String id;
	private int formControlType;
	private int returnType;
	private int widgetType;
	private String longText;
	private String shortText;
	private String header;
	private boolean relevant;
	private boolean required = false;
	private Object defaultValue;
	private Object value;
	private int selectedIndex = -1;
	private String hint;
	private SimpleOrderedHashtable selectMap;
	private String xpathBinding;
	private String relevantString;
	private String bindID;
	private int labelPosition;
	private Binding bind;

	public Binding getBind() {
		return bind;
	}

	public void setBind(Binding bind) {
		this.bind = bind;
	}

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
		if (this.bind != null)
			return this.bind.isRequired();
		else{
			System.out.println(this.getBindID()+" bind null");
			return false;
		}
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

	public Object getValueByTypeFromString(String value) {
		Object result = null;
		switch (this.returnType) {
		case Constants.RETURN_INTEGER:
			result = (Object) new Integer(Integer.parseInt(value));
			break;
		case Constants.RETURN_STRING:
			result = value;
			break;
		case Constants.RETURN_DATE:
			result = new Date();
			result = (Date) J2MEUtil.getDateFromString(value);
			System.out.println("set string date to" +result.toString());
			break;
		case Constants.RETURN_SELECT1:
			result = value;
			break;
		case Constants.RETURN_SELECT_MULTI:
			result = value;
			break;
		case Constants.RETURN_BOOLEAN:
			result = value;
			break;

		default:
			break;
		}
		
		return result;	
		
		
		
		
	}	
	
	public int getLabelPosition() {
		return labelPosition;
	}
	
	public void setLabelPosition(int labelPosition) {
		this.labelPosition = labelPosition;
	}
	
}
