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

package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a selection
 * of one and only one item from a list
 * 
 * @author Drew Roos
 *
 */
public class SelectOneData implements IAnswerData {
	Selection s;
	
	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public SelectOneData() {
		
	}
	
	public SelectOneData (Selection s) {
		setValue(s);
	}
	
	public IAnswerData clone () {
		return new SelectOneData(s.clone());
	}
	
	public void setValue (Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		s = (Selection)o;
	}
	
	public Object getValue () {
		return s;
	}
	
	public String getDisplayText () {
		return s.getText();
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		s = (Selection)ExtUtil.read(in, Selection.class, pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, s);
	}
}
