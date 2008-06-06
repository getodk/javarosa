package org.javarosa.clforms.api;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Enumeration;

import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.List;

import org.javarosa.clforms.util.J2MEUtil;
import org.javarosa.clforms.util.SimpleOrderedHashtable;
import org.javarosa.dtree.i18n.*;

public class Prompt implements ILocalizable {

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
	private SimpleOrderedHashtable localizedSelectMap;
	private String xpathBinding;
	private String relevantString;
	private String bindID;
	private int labelPosition;
	private Binding bind;

    private String appearanceString;
	private String typeString;
	private SimpleOrderedHashtable selectGraphDataMap;
    private int [] controlDataArray;
    
    private String longTextId = null;
    private String shortTextId = null;
    private String hintTextId = null;
    private String selectMapId = null;
	
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
            if(selectedIndex == -1 && localizedSelectMap != null && value != null) {
                int i = 0;

                Enumeration itr = localizedSelectMap.keys();
                while(itr.hasMoreElements()) {
                    String key = (String)itr.nextElement();

                    if(value.equals((String)localizedSelectMap.get(key))) {
                        selectedIndex = i;
                        break;
                    }

                    i++;
                }
            }    
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

	private void updateLocalizedSelectMap(ILocalizer localizer){
        if(selectMap == null) 
            return;
        if(localizedSelectMap != null) {
            localizedSelectMap.clear();
        }
        
        Enumeration itr = selectMap.keys();
        			
        	while(itr.hasMoreElements()){
        		String key = (String)itr.nextElement();
                String localizedKey = localizer.getText(key);
                
                if(localizedKey == null ||localizedKey.equals("")) {
                    ILocalizer defaultLocalizer = XFormsLocaleManager.getDefaultLocalizer();
                    localizedKey = defaultLocalizer.getText(key);
                }
                String value = (String) selectMap.get(key);
                if(localizedKey != null){
                    	localizedSelectMap.put(localizedKey, value);
                } else {
                        localizedSelectMap.put(key, value);
                }
        	}
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

	public SimpleOrderedHashtable getLocalizedSelectMap() {
		return localizedSelectMap;
	}
	
	public SimpleOrderedHashtable getSelectMap() {
		return selectMap;
	}

	public void setSelectMap(SimpleOrderedHashtable selectMap) {
		this.selectMap = selectMap;
        this.localizedSelectMap = new SimpleOrderedHashtable();
	}

	public void setSelectLabelValue(String label, String value){
        this.selectMap.put(label, value);
    }

	public void setSelectLabelValue(String label, String value, 
		ILocalizer localizer){
		this.selectMap.put(label, value);
		updateLocalizedSelectMap(localizer);
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

    public String getLongTextId() {
        return longTextId;
    }
    
     public void setLongTextId(String textId, ILocalizer localizer) {
            longTextId = textId + ";long";
        if(localizer != null) {
            longText =  localizer.getText(longTextId );
        }
    }
     
    public String getShortTextId() {
        return shortTextId;
    }
    
    public void setShortTextId(String textId, ILocalizer localizer) {
        shortTextId = textId + ";short";
        //localizer.addAvailableLocale()
        
        if(localizer != null) {
            shortText = localizer.getText(shortTextId );        
        }
    } 
    
    public String getHintTextId() {
        return hintTextId;
    }
    
    public void setHintTextId(String textId, ILocalizer localizer) {
        hintTextId = textId + ";hint";
        if(localizer != null) {
            hint = localizer.getText(hintTextId);
        }
    }
    
    public String getSelectMapId() {
        return selectMapId;
    }
    
    public void setSelectMapId(String textId, ILocalizer localizer) {
        selectMapId = textId;
        if(localizer != null) {
            selectMap = localizer.getSelectMap(selectMapId);
        }
    }
    
    public String getLocalizedLabel(String labelId, ILocalizer localizer) {
        String localizedLabel = null;
        if(localizer != null)
            localizedLabel = localizer.getText(labelId);
        return localizedLabel;
    }
    
    public void localeChanged(String locale, ILocalizer localizer) {
       updateLocalizedSelectMap(localizer);
        if(longTextId != null) {
           longText = localizer.getText(longTextId);
           if(longText != null && longText.equals("")) {
               longText = XFormsLocaleManager.getDefaultLocalizer().getText(longTextId);
           }
        }

        if(shortTextId != null) {
            shortText = localizer.getText(shortTextId);
            if(shortText != null && shortText.equals("")) {
               shortText = XFormsLocaleManager.getDefaultLocalizer().getText(shortTextId);
            }
        }

        if(hintTextId != null) {
            hint = localizer.getText(hintTextId);
            if(hint != null && hint.equals("")) {
               hint = XFormsLocaleManager.getDefaultLocalizer().getText(hintTextId);
            }
        }
        
//        if(value != null) {
//            if(this.formControlType == Constants.SELECT1) {
//                if(getSelectedIndex() != -1) {
//                    String selectValue = (String) localizedSelectMap.keyAt(selectedIndex);
//            	setValue(selectValue); 
//                }
//            }
//            
//      }
    }
}