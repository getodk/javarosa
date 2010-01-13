/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.services.locale.Localizable;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.util.OrderedHashtable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/** 
 * The definition of a Question to be presented to users when
 * filling out a form.
 * 
 * QuestionDef requires that any IDataReferences that are used
 * are contained in the FormDefRMS's PrototypeFactoryDeprecated in order
 * to be properly deserialized. If they aren't, an exception
 * will be thrown at the time of deserialization. 
 * 
 * @author Daniel Kayiwa/Drew Roos
 *
 */
public class QuestionDef implements IFormElement, Localizable {
	private int id;
	private IDataReference binding;	/** reference to a location in the model to store data in */
	
	private int controlType;  /* The type of widget. eg TextInput,Slider,List etc. */
	private String appearanceAttr;
	
	private String longText;	 /* The prompt text. The text the user sees. */
	private String longTextID;
	private String shortText;	 /* The prompt text. The text the user sees in short modes. */
	private String shortTextID;
	private String helpText;	 /* The help text. */
	private String helpTextID;

	
	private OrderedHashtable selectItems;  	/** String -> String */
	private OrderedHashtable selectItemIDs;	/** String -> String */
	private Vector selectItemsLocalizable;
	
	//this may be used in the future, but it is not the default value you're probably thinking about
	//"these are not the default values you are looking for..."
	//"not your mothers's default value anymore!"
	//private IAnswerData defaultValue;
		
	Vector observers;
	
	public QuestionDef () {
		this(Constants.NULL_ID, Constants.DATATYPE_TEXT);
	}
	
	public QuestionDef (int id, int controlType) {
		setID(id);
		setControlType(controlType);
		observers = new Vector();
	}
		
	public boolean equals (Object o) {
		if (o instanceof QuestionDef) {
			QuestionDef q = (QuestionDef)o;
			return (id == q.id &&
					ExtUtil.equals(binding, q.binding) &&
					controlType == q.controlType &&
					ExtUtil.equals(appearanceAttr, q.appearanceAttr) &&
					ExtUtil.equals(longText, q.longText) &&
					ExtUtil.equals(longTextID, q.longTextID) &&
					ExtUtil.equals(shortText, q.shortText) &&
					ExtUtil.equals(shortTextID, q.shortTextID) &&
					ExtUtil.equals(helpText, q.helpText) &&
					ExtUtil.equals(helpTextID, q.helpTextID) &&
					ExtUtil.equals(ExtUtil.nullIfEmpty(selectItemIDs), ExtUtil.nullIfEmpty(q.selectItemIDs)) &&
					ExtUtil.equals(ExtUtil.nullIfEmpty(selectItemsLocalizable), ExtUtil.nullIfEmpty(q.selectItemsLocalizable))
					);
				//no defaultValue, selectItems
		} else {
			return false;
		}
	}
	
	public int getID () {
		return id;
	}
	
	public void setID (int id) {
		this.id = id;
	}
	
	public IDataReference getBind() {
		return binding;
	}
	
	public void setBind(IDataReference binding) {
		this.binding = binding;
	}
	
	public int getControlType() {
		return controlType;
	}
	
	public void setControlType(int controlType) {
		this.controlType = controlType;
	}

	public String getAppearanceAttr () {
		return appearanceAttr;
	}
	
	public void setAppearanceAttr (String appearanceAttr) {
		this.appearanceAttr = appearanceAttr;
	}	
	
	public String getLongText () {
		return longText;
	}
	
	public void setLongText (String longText) {
		this.longText = longText;
	}

    public String getLongTextID () {
        return longTextID;
    }
    
    public void setLongTextID (String textID, Localizer localizer) {
    	this.longTextID = textID;
    	if (localizer != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}
    }
	
	public String getShortText () {
		return shortText;
	}
	
	public void setShortText (String shortText) {
		this.shortText = shortText;
	}

    public String getShortTextID () {
        return shortTextID;
    }

    public void setShortTextID (String textID, Localizer localizer) {
    	this.shortTextID = textID;
    	if (localizer != null) {
    		shortText = localizer.getLocalizedText(shortTextID);        
    	}
    } 

	public String getHelpText () {
		return helpText;
	}
	
	public void setHelpText (String helpText) {
		this.helpText = helpText;
	}

    public String getHelpTextID () {
        return helpTextID;
    }
    
    public void setHelpTextID (String textID, Localizer localizer) {
        this.helpTextID = textID;
        if (localizer != null) {
            helpText = localizer.getLocalizedText(helpTextID);
        }
    }

	public OrderedHashtable getSelectItems () {
		return selectItems;
	}

	//this function is dangerous: QuestionDef will not serialize properly unless selectItemIDs is set as well
	public void setSelectItems (OrderedHashtable selectItems) {
		this.selectItems = selectItems;
	}
	
	//this function is dangerous: QuestionDef will not serialize properly unless selectItemIDs is set as well
	public void addSelectItem (String label, String value) {
		if (selectItems == null)
			selectItems = new OrderedHashtable();
		selectItems.put(label, value);
	}
	
	public OrderedHashtable getSelectItemIDs () {
		return selectItemIDs;
	}
	
	public Vector getSelectItemsLocalizable () {
		return selectItemsLocalizable;
	}
	
	public void setSelectItemIDs (OrderedHashtable selectItemIDs, Vector selectItemsLocalizable, Localizer localizer) {
		this.selectItemIDs = selectItemIDs;
		this.selectItemsLocalizable = selectItemsLocalizable;
		if(localizer != null) {
			localizeSelectMap(localizer);
		}
	}
	
	public void addSelectItemID (String labelID, boolean type, String value) {
		if (selectItemIDs == null) {
			selectItemIDs = new OrderedHashtable();
			selectItemsLocalizable = new Vector();
		}
		selectItemIDs.put(labelID, value);
		selectItemsLocalizable.addElement(new Boolean(type));
	}
	
	//calling when localizer == null is meant for when there is no localization data and selectIDMap contains only
	//fixed strings (trans is always false)
	public void localizeSelectMap (Localizer localizer) {
		selectItems = null;
		
		String label;
		for (int i = 0; i < selectItemIDs.size(); i++) {
			String key = (String)selectItemIDs.keyAt(i);
			boolean translate = ((Boolean)selectItemsLocalizable.elementAt(i)).booleanValue();
			if (translate) {
				label = (localizer == null ? "[itext:" + i + "]" : localizer.getLocalizedText(key));
			} else {
				label = key;
			}
			addSelectItem(label, (String)selectItemIDs.get(key));
		}
	}
  
	public int getSelectedItemIndex(String value) {
		if (selectItems != null) {
			for (int i = 0; i < selectItems.size(); i++) {
				if (((String)selectItems.elementAt(i)).equals(value)) {
					return i;
				}
			}
		}
		return -1;
	}
		
	/*
	public IAnswerData getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(IAnswerData defaultValue) {
		this.defaultValue = defaultValue;
	}
    */

    public void localeChanged(String locale, Localizer localizer) {
    	if(longTextID != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}

    	if(shortTextID != null) {
    		shortText = localizer.getLocalizedText(shortTextID);
    	}

    	if(helpTextID != null) {
    		helpText = localizer.getLocalizedText(helpTextID);
    	}
    	
    	if (selectItemIDs != null) {
    		localizeSelectMap(localizer);
    	}
    	
    	alertStateObservers(FormElementStateListener.CHANGE_LOCALE);
    }
	
	public Vector getChildren () {
		return null;
	}
	
	public void setChildren (Vector v) {
		throw new IllegalStateException();
	}
	
	public void addChild (IFormElement fe) {
		throw new IllegalStateException();
	}
	
	public IFormElement getChild (int i) {
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		setID(ExtUtil.readInt(dis));
		setAppearanceAttr((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setLongText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setShortText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setHelpText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setLongTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);
		setShortTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);
		setHelpTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);

		setControlType(ExtUtil.readInt(dis));

		setSelectItemIDs(
				(OrderedHashtable)ExtUtil.nullIfEmpty((OrderedHashtable)ExtUtil.read(dis, new ExtWrapMap(String.class, String.class, true), pf)),
				ExtUtil.nullIfEmpty((Vector)ExtUtil.read(dis, new ExtWrapList(Boolean.class), pf)),
				null);
		if (getSelectItemIDs() != null && (controlType == Constants.CONTROL_SELECT_MULTI || controlType == Constants.CONTROL_SELECT_ONE)) {
			localizeSelectMap(null); //even for non-multilingual forms, text must be initially 'localized'
		}

		binding = (IDataReference)ExtUtil.read(dis, new ExtWrapNullable(new ExtWrapTagged()), pf);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapNullable(getAppearanceAttr()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongText()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortText()));
		ExtUtil.write(dos, new ExtWrapNullable(getHelpText()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getHelpTextID()));
				
		ExtUtil.writeNumeric(dos, getControlType());
		
		//selectItems should not be serialized
		ExtUtil.write(dos, new ExtWrapMap(ExtUtil.emptyIfNull(getSelectItemIDs())));
		ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(selectItemsLocalizable)));

		ExtUtil.write(dos, new ExtWrapNullable(binding == null ? null : new ExtWrapTagged(binding)));
	}

	/* === MANAGING OBSERVERS === */
	
	public void registerStateObserver (FormElementStateListener qsl) {
		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}
	
	public void unregisterStateObserver (FormElementStateListener qsl) {
		observers.removeElement(qsl);
	}
	
	public void unregisterAll () {
		observers.removeAllElements();
	}
	
	public void alertStateObservers (int changeFlags) {
		for (Enumeration e = observers.elements(); e.hasMoreElements(); )
			((FormElementStateListener)e.nextElement()).formElementStateChanged(this, changeFlags);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormElement#getDeepChildCount()
	 */
	public int getDeepChildCount() {
		return 1;
	}
}