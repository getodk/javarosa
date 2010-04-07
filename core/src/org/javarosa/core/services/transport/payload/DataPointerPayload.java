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

package org.javarosa.core.services.transport.payload;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A payload for a Pointer to some data.
 *  
 * @author Clayton Sims
 * @date Dec 29, 2008 
 *
 */
public class DataPointerPayload implements IDataPayload {
	IDataPointer pointer;
	
	/**
	 * Note: Only useful for serialization.
	 */
	public DataPointerPayload() {
	}
	
	public DataPointerPayload(IDataPointer pointer) {
		this.pointer = pointer;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#accept(org.javarosa.core.services.transport.IDataPayloadVisitor)
	 */
	public Object accept(IDataPayloadVisitor visitor) {
		return visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getLength()
	 */
	public long getLength() {
		//Unimplemented. This method will eventually leave the contract
		return pointer.getLength();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadId()
	 */
	public String getPayloadId() {
		return pointer.getDisplayText();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadStream()
	 */
	public InputStream getPayloadStream() {
		return pointer.getDataStream();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadType()
	 */
	public int getPayloadType() {
		//TODO: FIX so this isn't always the case
		return IDataPayload.PAYLOAD_TYPE_JPG;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		pointer = (IDataPointer)ExtUtil.read(in, new ExtWrapTagged());
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(pointer));
	}
	
	public int getTransportId() {
		return -1;
	}

}
