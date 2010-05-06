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
import java.util.Vector;

import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a selection of
 * any number of items from a list.
 * 
 * @author Drew Roos
 *
 */
public class SelectMultiData implements IAnswerData {
	Vector vs; //vector of Selection
	
	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public SelectMultiData() {
		
	}
	
	public SelectMultiData (Vector vs) {
		setValue(vs);
	}
	
	public IAnswerData clone () {
		Vector v = new Vector();
		for (int i = 0; i < vs.size(); i++) {
			v.addElement(((Selection)vs.elementAt(i)).clone());
		}
		return new SelectMultiData(v);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
	public void setValue (Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		
		vs = vectorCopy((Vector)o);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
	public Object getValue () {
		return vectorCopy(vs);
	}
	
	/**
	 * @return A type checked vector containing all of the elements
	 * contained in the vector input
	 * TODO: move to utility class
	 */
	private Vector vectorCopy(Vector input) {
		Vector output = new Vector();
		//validate type
		for (int i = 0; i < input.size(); i++) {
			Selection s = (Selection)input.elementAt(i);
			output.addElement(s);
		}
		return output;
	}
	/**
	 * @return THE XMLVALUE!!
	 */
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
	public String getDisplayText () {
		String str = "";
		
		for (int i = 0; i < vs.size(); i++) {
			Selection s = (Selection)vs.elementAt(i);
			str += s.getValue();
			if (i < vs.size() - 1)
				str += ", ";
		}
		
		return str;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		vs = (Vector)ExtUtil.read(in, new ExtWrapList(Selection.class), pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapList(vs));
	}
}
