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

/**
 *
 */
package org.javarosa.core.model.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 * @date May 19, 2009
 *
 */
public class BooleanData implements IAnswerData {

	boolean data;

	/**
	 * NOTE: ONLY FOR SERIALIZATION
	 */
	public BooleanData() {

	}

	public BooleanData(boolean data) {
		this.data = data;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#clone()
	 */
    @Override
	public IAnswerData clone() {
		return new BooleanData(data);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getDisplayText()
	 */
    @Override
	public String getDisplayText() {
		if(data) {
			return "True";
		} else {
			return "False";
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#getValue()
	 */
    @Override
	public Object getValue() {
		return new Boolean(data);
	}


	/* (non-Javadoc)
	 * @see org.javarosa.core.model.data.IAnswerData#setValue(java.lang.Object)
	 */
    @Override
	public void setValue(Object o) {
		data = ((Boolean)o).booleanValue();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
    @Override
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		data = in.readBoolean();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
    @Override
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeBoolean(data);
	}

    @Override
	public UncastData uncast() {
		return new UncastData(data ? "1" : "0");
	}

    @Override
	public BooleanData cast(UncastData data) throws IllegalArgumentException {
		if("1".equals(data)) {
			return new BooleanData(true);
		}

		if("0".equals(data)) {
			return new BooleanData(false);
		}

		throw new IllegalArgumentException("Invalid cast of data [" + data.value + "] to type Boolean");
	}
}
