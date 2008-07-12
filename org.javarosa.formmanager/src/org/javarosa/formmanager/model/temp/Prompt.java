package org.javarosa.formmanager.model.temp;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.clforms.api.Binding;
import org.javarosa.clforms.api.Constants;
import org.javarosa.clforms.util.J2MEUtil;
import org.javarosa.clforms.util.SimpleOrderedHashtable;
import org.javarosa.dtree.i18n.ILocalizable;
import org.javarosa.dtree.i18n.Localizer;
import org.javarosa.formmanager.utility.QuestionStateListener;

public class Prompt implements ILocalizable {
	private String id;
	private int formControlType;
	private int returnType;
	private int widgetType;

	private String longText;
    private String longTextId;
	private String shortText;
    private String shortTextId;
	private String hintText;
    private String hintTextId;
	private SimpleOrderedHashtable selectMap;
	private SimpleOrderedHashtable selectIDMap;
	private Vector selectIDMapTrans; /* vector of booleans corresponding to selectIDMap: true = localizable text handle
									  *		 											 false = unlocalizable fixed string */
	private String header; //unused?
	
	private boolean relevant;
	private boolean required = false;
	private Object defaultValue;
	private Object value;
	private int selectedIndex = -1;
	private String xpathBinding;
	private String relevantString;
	private String bindID;
	private int labelPosition;
	private Binding bind;

    private String appearanceString;
	private String typeString;
	private SimpleOrderedHashtable selectGraphDataMap;
    private int [] controlDataArray;
    
    public void registerStateObserver (QuestionStateListener qsl) {
    	
    }

    public void unregisterStateObserver (QuestionStateListener qsl) {
    	
    }
    
	public Binding getBind() {
		return bind;
	}

	public void setBind(Binding bind) {
		this.bind = bind;
	}

	public Prompt() {
		super();
		relevantString = null;
		appearanceString = null;
		typeString = null;
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

    public String getLongTextId() {
        return longTextId;
    }
    
    public void setLongTextId(String textId, Localizer localizer) {
    	this.longTextId = textId;
    	if(localizer != null) {
    		longText = getLocalizedText(localizer, longTextId);
    	}
    }
    
	public String getShortText() {
		return shortText;
	}

	public void setShortText(String shortText) {
		this.shortText = shortText;
	}

    public String getShortTextId() {
        return shortTextId;
    }

    public void setShortTextId(String textId, Localizer localizer) {
    	this.shortTextId = textId;
    	if(localizer != null) {
    		shortText = getLocalizedText(localizer, shortTextId);        
    	}
    } 

	public String getHint() {
		return hintText;
	}

	public void setHint(String hintText) {
		this.hintText = hintText;
	}

    public String getHintTextId() {
        return hintTextId;
    }
    
    public void setHintTextId(String textId, Localizer localizer) {
        this.hintTextId = textId;
        if(localizer != null) {
            hintText = getLocalizedText(localizer, hintTextId);
        }
    }

	public SimpleOrderedHashtable getSelectMap() {
		return selectMap;
	}

	public void setSelectMap(SimpleOrderedHashtable selectMap) {
		this.selectMap = selectMap;
	}
	
	public void addSelectItem (String label, String value) {
		if (selectMap == null)
			selectMap = new SimpleOrderedHashtable();
		selectMap.put(label, value);
	}
	
	public SimpleOrderedHashtable getSelectIDMap () {
		return selectIDMap;
	}
	
	public Vector getSelectIDMapTrans () {
		return selectIDMapTrans;
	}
	
	public void setSelectIDMap (SimpleOrderedHashtable selectIDMap, Vector selectIDMapTrans, Localizer localizer) {
		this.selectIDMap = selectIDMap;
		this.selectIDMapTrans = selectIDMapTrans;
		if (localizer != null) {
			localizeSelectMap(localizer);
		}
	}
	
	public void addSelectItemID (String labelID, boolean type, String value) {
		if (selectIDMap == null) {
			selectIDMap = new SimpleOrderedHashtable();
			selectIDMapTrans = new Vector();
		}
		selectIDMap.put(labelID, value);
		selectIDMapTrans.addElement(new Boolean(type));
	}
	
	//calling when localizer == null is meant for when there is no localization data and selectIDMap contains only
	//fixed strings (trans is always false)
	public void localizeSelectMap (Localizer localizer) {
		selectMap = null;
		String label;
		for (int i = 0; i < selectIDMap.size(); i++) {
			String key = (String)selectIDMap.keyAt(i);
			boolean trans = ((Boolean)selectIDMapTrans.elementAt(i)).booleanValue();
			if (trans) {
				label = (localizer == null ? "[itext]" : getLocalizedText(localizer, key));
			} else {
				label = key;
			}
			addSelectItem(label, (String)selectIDMap.get(key));
		}
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
	
	public QuestionData getValue() {
		return null; //value;
	}

	public void setValue(Object value) {
		this.value = value;
		
		if (selectMap != null && value != null) {
			selectedIndex = -1;
			
			Enumeration e;
			int i;
			for (e = selectMap.keys(), i = 0;  e.hasMoreElements(); i++) {
				String key = (String)e.nextElement();

				if(value.equals((String)selectMap.get(key))) {
					selectedIndex = i;
					break;
				}
			}
			
			if (selectedIndex == -1)
				System.out.println("warning: value set that does not exist in select");
		}    
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
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
	
    public void setAppearanceString(String appearance) {
        this.appearanceString = appearance;
    }
 
    public String getAppearanceString() {
        return this.appearanceString;
    }

    public void setTypeString(String type) {
        this.typeString = type;
    }

    public String getTypeString() {
        return this.typeString;
    }
   
    public int[] getControlDataArray() {
        return controlDataArray;
    }
    
    public void setControlDataArray(int [] controlDataArray) {
        this.controlDataArray = controlDataArray;
    }
    
    private String getLocalizedText (Localizer localizer, String textID) {
    	String text = localizer.getText(textID);
    	if (text == null)
    		throw new RuntimeException("can't find localized text for current locale! text id: [" + textID + "]");
    	return text;
    }
    
    public void localeChanged(String locale, Localizer localizer) {
    	if(longTextId != null) {
    		longText = getLocalizedText(localizer, longTextId);
    	}

    	if(shortTextId != null) {
    		shortText = getLocalizedText(localizer, shortTextId);
    	}

    	if(hintTextId != null) {
    		hintText = getLocalizedText(localizer, hintTextId);
    	}
    	
    	if (selectIDMap != null) {
    		localizeSelectMap(localizer);
    	}
    }
    
	// @JJ May 26, 2008: added
	/**
	 * Determines if the value of this prompt is empty.  this method is not complete
	 * @return
	 */
	public boolean isEmpty() {
		if(this.value == null || this.value.equals(""))
		{
			return true;
		}
		else
		{
		    return false;
		}
	}
}