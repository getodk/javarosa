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
import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.model.utils.DateUtils;
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
	private List<IFormElement> children;	/** A list of questions on a group. */
	private boolean repeat;  /** True if this is a "repeat", false if it is a "group" */
	private int id;	/** The group number. */
	private IDataReference binding;	/** reference to a location in the model to store data in */
    private List<TreeElement> additionalAttributes = new ArrayList<TreeElement>(0);

	private String labelInnerText;
	private String appearanceAttr;
	private String textID;

	//custom phrasings for repeats
	public String chooseCaption;
	public String addCaption;
	public String delCaption;
	public String doneCaption;
	public String addEmptyCaption;
	public String doneEmptyCaption;
	public String entryHeader;
	public String delHeader;
	public String mainHeader;

	List<FormElementStateListener> observers;

	public boolean noAddRemove = false;
	public IDataReference count = null;

	public GroupDef () {
		this(Constants.NULL_ID, null, false);
	}

	public GroupDef(int id, List<IFormElement> children, boolean repeat) {
		setID(id);
		setChildren(children);
		setRepeat(repeat);
		observers = new ArrayList<FormElementStateListener>(0);
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

	public void setAdditionalAttribute(String namespace, String name, String value) {
		TreeElement.setAttribute(null, additionalAttributes, namespace, name, value);
	}

	public String getAdditionalAttribute(String namespace, String name) {
		TreeElement e = TreeElement.getAttribute(additionalAttributes, namespace, name);
		if ( e != null ) {
			return e.getAttributeValue();
		}
		return null;
	}

	public List<TreeElement> getAdditionalAttributes() {
		return additionalAttributes;
	}

	public List<IFormElement> getChildren() {
		return children;
	}

	public void setChildren (List<IFormElement> children) {
		this.children = (children == null ? new ArrayList<IFormElement>(0) : children);
	}

	public void addChild (IFormElement fe) {
		children.add(fe);
	}

	public IFormElement getChild (int i) {
		if (children == null || i >= children.size()) {
			return null;
		} else {
			return children.get(i);
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

	public String getLabelInnerText() {
		return labelInnerText;
	}

	public void setLabelInnerText(String lit){
		labelInnerText = lit;
	}


	public String getAppearanceAttr () {
		return appearanceAttr;
	}

	public void setAppearanceAttr (String appearanceAttr) {
		this.appearanceAttr = appearanceAttr;
	}

    public void localeChanged(String locale, Localizer localizer) {
       for (IFormElement child : children) {
          child.localeChanged(locale, localizer);
       }
    }

    public IDataReference getCountReference() {
    	return count;
    }

    public TreeReference getConextualizedCountReference(TreeReference context) {
    	return FormInstance.unpackReference(count).contextualize(context);
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
      for (IFormElement child : children) {
         total += child.getDeepChildCount();
      }
		return total;
	}

	/** Reads a group definition object from the supplied stream. */
	public void readExternal(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
		try {
			setID(ExtUtil.readInt(dis));
			setAppearanceAttr((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
			setBind((IDataReference)ExtUtil.read(dis, new ExtWrapTagged(), pf));
			setTextID((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
			setLabelInnerText((String)ExtUtil.read(dis, new ExtWrapNullable(String.class), pf));
			setRepeat(ExtUtil.readBool(dis));
			setChildren((List<IFormElement>)ExtUtil.read(dis, new ExtWrapListPoly(), pf));
	
			noAddRemove = ExtUtil.readBool(dis);
			count = (IDataReference)ExtUtil.read(dis, new ExtWrapNullable(new ExtWrapTagged()), pf);
	
			chooseCaption = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			addCaption = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			delCaption = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			doneCaption = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			addEmptyCaption = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			doneEmptyCaption = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			entryHeader = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			delHeader = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
			mainHeader = ExtUtil.nullIfEmpty(ExtUtil.readString(dis));
	
			additionalAttributes = ExtUtil.readAttributes(dis, null);
		} catch ( OutOfMemoryError e ) {
			throw new DeserializationException("serialization format change caused misalignment and out-of-memory error");
		}
	}

	/** Write the group definition object to the supplied stream. */
	public void writeExternal(DataOutputStream dos) throws IOException {
		ExtUtil.writeNumeric(dos, getID());
		ExtUtil.write(dos, new ExtWrapNullable(getAppearanceAttr()));
		ExtUtil.write(dos, new ExtWrapTagged(getBind()));
		ExtUtil.write(dos, new ExtWrapNullable(getTextID()));
		ExtUtil.write(dos, new ExtWrapNullable(getLabelInnerText()));
		ExtUtil.writeBool(dos, getRepeat());
		ExtUtil.write(dos, new ExtWrapListPoly(getChildren()));

		ExtUtil.writeBool(dos, noAddRemove);
		ExtUtil.write(dos, new ExtWrapNullable(count != null ? new ExtWrapTagged(count) : null));

		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(chooseCaption));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(addCaption));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(delCaption));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(doneCaption));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(addEmptyCaption));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(doneEmptyCaption));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(entryHeader));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(delHeader));
		ExtUtil.writeString(dos, ExtUtil.emptyIfNull(mainHeader));

		ExtUtil.writeAttributes(dos, additionalAttributes);
	}

	public void registerStateObserver (FormElementStateListener qsl) {
		if (!observers.contains(qsl)) {
			observers.add(qsl);
		}
	}

	public void unregisterStateObserver (FormElementStateListener qsl) {
		observers.remove(qsl);
	}

	public String getTextID() {
		return textID;
	}

	public void setTextID(String textID) {
		if(textID==null){
			this.textID = null;
			return;
		}
		if(DateUtils.stringContains(textID,";")){
			System.err.println("Warning: TextID contains ;form modifier:: \""+textID.substring(textID.indexOf(";"))+"\"... will be stripped.");
			textID=textID.substring(0, textID.indexOf(";")); //trim away the form specifier
		}
		this.textID = textID;
	}
}
