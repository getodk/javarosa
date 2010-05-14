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
	
	public String getDisplayText() {
		String toReturn = "";
		for (int i=0; i < data.length; i++) {
			if (i != 0) { 
				toReturn += ", ";
			}
			toReturn += data[i].getDisplayText();
		}
		return toReturn;
	}

	public Object getValue() {
		return data;
	}

	public void setValue(Object o) {
		if(o == null) {
			throw new NullPointerException("Attempt to set an IAnswerData class to null.");
		}
		data = (IDataPointer[]) o;
	}

	public IAnswerData clone () {
		return null; //not cloneable
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		int length = in.readInt();
		data = new IDataPointer[length];
		for(int i = 0; i < data.length; ++i) {
			data[i] = (IDataPointer)ExtUtil.read(in, new ExtWrapTagged());
		}
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(data.length);
		for(int i = 0; i < data.length ; ++i ) {
			ExtUtil.write(out, new ExtWrapTagged(data[i]));
		}
	}

	public UncastData uncast() {
		String ret = "";
		for(IDataPointer datum : data) {
			ret += datum.getDisplayText() + " ";
		}
		if(ret.length() > 0) {
			ret = ret.substring(0, ret.length() -1);
		}
		return new UncastData(ret);
	}
}
