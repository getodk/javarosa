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

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A response to a question requesting a String Value
 * @author Drew Roos
 *
 */
public class StringData implements IAnswerData {
	private String s;

	/**
	 * Empty Constructor, necessary for dynamic construction during deserialization.
	 * Shouldn't be used otherwise.
	 */
	public StringData() {

	}

	public StringData (String s) {
		setValue(s);
	}

    @Override
	public IAnswerData clone () {
		return new StringData(s);
	}

	//string should not be null or empty; the entire StringData reference should be null in this case
    @Override
	public void setValue (Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		s = (String)o;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
    @Override
	public Object getValue () {
		return s;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
    @Override
	public String getDisplayText () {
		return s;
	}
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
    @Override
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		s = ExtUtil.readString(in);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
    @Override
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, s);
	}

    @Override
	public UncastData uncast() {
		return new UncastData(s);
	}

    @Override
	public StringData cast(UncastData data) throws IllegalArgumentException {
		return new StringData(data.value);
	}
}
