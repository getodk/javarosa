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
 * An answer data storing multiple pointers
 * @author Cory Zue
 *
 */
public class MultiPointerAnswerData implements IAnswerData {

	private IDataPointer[] data;

	/**
	 * NOTE: Only for serialization/deserialization
	 */
	public MultiPointerAnswerData() {
		//Only for serialization/deserialization
	}

	public MultiPointerAnswerData (IDataPointer[] values) {
		data = values;
	}

    @Override
	public String getDisplayText() {
		StringBuilder b = new StringBuilder();
		for (int i=0; i < data.length; i++) {
			if (i != 0) {
				b.append(", ");
			}
			b.append(data[i].getDisplayText());
		}
		return b.toString();
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
		data = (IDataPointer[]) o;
	}

    @Override
	public IAnswerData clone () {
		return null; //not cloneable
	}

    @Override
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		int length = in.readInt();
		data = new IDataPointer[length];
		for(int i = 0; i < data.length; ++i) {
			data[i] = (IDataPointer)ExtUtil.read(in, new ExtWrapTagged());
		}
	}

    @Override
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(data.length);
		for(int i = 0; i < data.length ; ++i ) {
			ExtUtil.write(out, new ExtWrapTagged(data[i]));
		}
	}

    @Override
	public UncastData uncast() {
		StringBuilder b = new StringBuilder();
		for(IDataPointer datum : data) {
			b.append(datum.getDisplayText());
			b.append(" ");
		}
		return new UncastData(b.toString().trim());
	}

    @Override
	public MultiPointerAnswerData cast(UncastData data) throws IllegalArgumentException {
		return null;
	}
}
