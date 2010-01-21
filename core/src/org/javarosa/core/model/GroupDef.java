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
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/** The definition of a group in a form or questionaire. 
 * 
 * @author Daniel Kayiwa
 *
 */
public class GroupDef implements IFormElement, Localizable {
	private Vector children;	/** A list of questions on a group. */	
	private boolean repeat;  /** True if this is a "repeat", false if it is a "group" */
	private int id;	/** The group number. */
	private IDataReference binding;	/** reference to a location in the model to store data in */
	
	private String longText;
	private String longTextID;
	private String shortText;
	private String shortTextID;
	
	Vector observers;
	
	public boolean noAddRemove = false;
	//public boolean startEmpty = false;
	public IDataReference count = null;
	
	public GroupDef () {
		this(Constants.NULL_ID, null, false);
	}
	
	public GroupDef(int id, Vector children, boolean repeat) {
		setID(id);
		setChildren(children);
		setRepeat(repeat);
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
	
	public Vector getChildren() {
		return children;
	}

	public void setChildren (Vector children) {
		this.children = (children == null ? new Vector() : children);
	}
	
	public void addChild (IFormElement fe) {
		children.addElement(fe);
	}
	
	public IFormElement getChild (int i) {
		if (children == null || i >= children.size()) {
			return null;
		} else {
			return (IFormElement)children.elementAt(i);
		}
	}
	
	/**
	 * @return true if this represents a <repeat> element
	 */
	public boolean getRepeat () {
		return repeat;
	}
	
	public void setRepeat (boolean repeat) {
		this.repeat = repeat;
	}
	
	public String getLongText () {
		return longText;
	}
	
	public void setLongText (String longText) {
		this.longText = longText;
	}

    /**
     * @return the iText id for the long text
     */
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

    /**
     * @return the iText id for the short text
     */
    public String getShortTextID () {
        return shortTextID;
    }

    public void setShortTextID (String textID, Localizer localizer) {
    	this.shortTextID = textID;
    	if (localizer != null) {
    		shortText = localizer.getLocalizedText(shortTextID);        
    	}
    } 
	
	/*
	public QuestionDef getQuestion(String varName){
		for(byte i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getVariableName().equals(varName))
				return def;
		}
		
		return null;
	}
	
	public QuestionDef getQuestionById(String id){
		for(byte i=0; i<getQuestions().size(); i++){
			QuestionDef def = (QuestionDef)getQuestions().elementAt(i);
			if(def.getId() == id)
				return def;
		}
		
		return null;
	}
	*/
    
    public void localeChanged(String locale, Localizer localizer) {
    	if(longTextID != null) {
    		longText = localizer.getLocalizedText(longTextID);
    	}

    	if(shortTextID != null) {
    		shortText = localizer.getLocalizedText(shortTextID);
    	}
    	
    	for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
    		((IFormElement)e.nextElement()).localeChanged(locale, localizer);
    	}
    }
    
    public IDataReference getCountReference() {
    	return count;
    }
	
	public String toString() {
		return "<group>";
	}
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.IFormElement#getDeepChildCount()
	 */
	public int getDeepChildCount() {
		int total = 0;
		Enumeration e = children.elements();
		while(e.hasMoreElements()) {
			total += ((IFormElement)e.nextElement()).getDeepChildCount();
		}
		return total;
	}

	/** Reads a group definition object from the supplied stream. */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		setID(ExtUtil.readInt(dis));
		setBind((IDataReference)ExtUtil.read(dis, new ExtWrapTagged(), pf));
		setLongText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setShortText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
		setLongTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);
		setShortTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf), null);
		setRepeat(ExtUtil.readBool(dis));
		setChildren((Vector)ExtUtil.read(dis, new ExtWrapListPoly(), pf));
		
		//TODO: custom group parameters
		//Clayton Sims - June 16, 2009: This change is necessary for having groups
		//be able to maintain "count" as opposed to "Add more?" questions, but will
		//_completely_ break RMS's that existed before this change that had groups.
		noAddRemove = ExtUtil.readBool(dis);
		count = (IDataReference)ExtUtil.read(dis, new ExtWrapTagged(new ExtWrapNullable(IDataReference.class)), pf);
	}

	/** Write the group definition object to the supplied stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapTagged(getBind()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongText()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortText()));
		ExtUtil.write(dos, new ExtWrapNullable(getLongTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getShortTextID()));				
		ExtUtil.writeBool(dos, getRepeat());
		ExtUtil.write(dos, new ExtWrapListPoly(getChildren()));

		//TODO: custom group parameters
		//Clayton Sims - June 16, 2009: This change is necessary for having groups
		//be able to maintain "count" as opposed to "Add more?" questions, but will
		//_completely_ break RMS's that existed before this change that had groups.
		ExtUtil.writeBool(dos, noAddRemove);
		//What if this is null?
		ExtUtil.write(dos,new ExtWrapTagged(new ExtWrapNullable(count)));
		
	}
	
	public void registerStateObserver (FormElementStateListener qsl) {
		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}
	
	public void unregisterStateObserver (FormElementStateListener qsl) {
		observers.removeElement(qsl);
	}
}
