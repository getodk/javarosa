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
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
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

	private Vector<SelectChoice> choices;
	private ItemsetBinding dynamicChoices;
	
	Vector observers;
	
	public QuestionDef () {
		this(Constants.NULL_ID, Constants.DATATYPE_TEXT);
	}
	
	public QuestionDef (int id, int controlType) {
		setID(id);
		setControlType(controlType);
		observers = new Vector();
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

	//not used during normal usage
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

	//not used during normal usage
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

	//not used during normal usage
    public String getHelpTextID () {
        return helpTextID;
    }
    
    public void setHelpTextID (String textID, Localizer localizer) {
        this.helpTextID = textID;
        if (localizer != null) {
            helpText = localizer.getLocalizedText(helpTextID);
        }
    }

    public void addSelectChoice (SelectChoice choice) {
    	if (choices == null) {
    		choices = new Vector<SelectChoice>();
    	}
    	choice.setIndex(choices.size());
    	choices.addElement(choice);
    }
    
    public Vector<SelectChoice> getChoices () {
    	return choices;
    }
    
    public SelectChoice getChoice (int i) {
    	return choices.elementAt(i);
    }
    
    public int getNumChoices () {
    	return (choices != null ? choices.size() : 0);
    }
    
	public SelectChoice getChoiceForValue (String value) {
		for (int i = 0; i < getNumChoices(); i++) {
			if (getChoice(i).getValue().equals(value)) {
				return getChoice(i);
			}
		}
		return null;
	}
	
	public ItemsetBinding getDynamicChoices () {
		return dynamicChoices;
	}
	
	public void setDynamicChoices (ItemsetBinding ib) {
		this.dynamicChoices = ib;
	}
	
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
    	
    	if (choices != null) {
    		for (int i = 0; i < choices.size(); i++) {
    			choices.elementAt(i).localeChanged(null, localizer);
    		}
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
		choices = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(dis, new ExtWrapList(SelectChoice.class), pf));
		for (int i = 0; i < getNumChoices(); i++) {
			choices.elementAt(i).setIndex(i);
		}
		dynamicChoices = (ItemsetBinding)ExtUtil.read(dis, new ExtWrapNullable(ItemsetBinding.class));

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
		
		ExtUtil.write(dos, new ExtWrapList(ExtUtil.emptyIfNull(choices)));
		ExtUtil.write(dos, new ExtWrapNullable(dynamicChoices));

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