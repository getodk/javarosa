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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * Basic implementor of the IDataPointer interface that keeps everything in memory
 * @author Cory Zue
 *
 */
public class BasicDataPointer implements IDataPointer {

	private byte[] data;
	private String name;
	
	/**
	 * NOTE: Only for serialization use.
	 */
	public BasicDataPointer() {
		//You shouldn't be calling this unless you are deserializing.
	}
	
	public BasicDataPointer(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#deleteData()
	 */
	public boolean deleteData() {
		
		this.data = null;
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getData()
	 */
	public byte[] getData() {
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getDisplayText()
	 */
	public String getDisplayText() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getDataStream()
	 */
	public InputStream getDataStream() {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		return bis;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		int size = in.readInt();
		if(size != -1) {
			data = new byte[size];
			in.read(data);
		}
		name = ExtUtil.readString(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		if(data == null || data.length < 0) {
			out.writeInt(-1);
		} else {
			out.writeInt(data.length);
			out.write(data);
		}
		ExtUtil.writeString(out, name);
	}

	public long getLength() {
		// TODO Auto-generated method stub
		return data.length;
	} 


}
