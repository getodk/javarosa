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

package org.javarosa.core.model.data.helper;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a selection
 * from a list. 
 * 
 * @author Drew Roos
 *
 */
public class Selection implements Externalizable {
	public String xmlValue = null;
	public int index = -1;
	
	/* in order to get localizable captions for this selection, the choice object must be the
	 * same object in the form model, or else it won't receive localization updates from form
	 * entry session
	 */
	public SelectChoice choice;
	
	/**
	 * for deserialization
	 */
	public Selection() {
		
	}
	
	public Selection (SelectChoice choice) {
		this.choice = choice;
		this.xmlValue = choice.getValue();
		this.index = choice.getIndex();
	}
	
	public Selection (String xmlValue) {
		this.xmlValue = xmlValue;		
	}
	
	public Selection clone () {
		Selection s = new Selection(xmlValue);
		s.choice = choice;

		return s;
	}
	
	public void attachQuestionDef(QuestionDef q) {
		this.question = q;
		this.qID = q.getID();
		
		if (xmlValue != null && xmlValue.length() > 0) {
			index =  q.getSelectedItemIndex(xmlValue); 			
		} else if (index != -1) {
			xmlValue = (String)q.getSelectItemIDs().elementAt(index);
		} else {
			throw new RuntimeException("insufficient data in selection");
		}
	}
	
	public String getText () {
		if (choice != null) {
			return choice.getCaption();
		} else {
			System.err.println("Warning!! Calling Selection.getText() when Choice object not linked!");
			return "[cannot access choice caption]";
		}
	}
	
	public String getValue () {
		if (xmlValue != null && xmlValue.length() > 0) {
			return xmlValue;
		} else {
			throw new RuntimeException("don't know xml value! perhaps selection was stored as index only and has not yet been linked up to a formdef?");
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		xmlValue = ExtUtil.readString(in);
		
		qID = ExtUtil.readInt(in);
		index = ExtUtil.readInt(in);
	}
 
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, getValue());
		
		ExtUtil.writeNumeric(out, qID);
		ExtUtil.writeNumeric(out, index);
	}
}
