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

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Answer data representing a pointer object.  The pointer is a reference to some
 * other object that it knows how to get out of memory.
 *
 * @author Cory Zue
 *
 */
public class PointerAnswerData implements IAnswerData {

	private IDataPointer data;


	/**
	 * NOTE: Only for serialization/deserialization
	 */
	public PointerAnswerData() {
		//Only for serialization/deserialization
	}

	public PointerAnswerData(IDataPointer data) {
		this.data = data;
	}

    @Override
	public IAnswerData clone () {
		return null; //not cloneable
	}

    @Override
	public String getDisplayText() {
		return data.getDisplayText();
	}

    @Override
	public Object getValue() {
		return data;
	}

    @Override
	public void setValue(Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		data = ((IDataPointer)o);
	}

    @Override
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		data = (IDataPointer)ExtUtil.read(in, new ExtWrapTagged());
	}

    @Override
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(data));
	}

    @Override
	public UncastData uncast() {
		return new UncastData(data.getDisplayText());
	}

    @Override
	public PointerAnswerData cast(UncastData data) throws IllegalArgumentException {
		return null;
	}
}
