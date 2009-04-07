/*
 * Copyright (C) 2009 JavaRosa-Core Project
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
package org.javarosa.core.services.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.api.Constants;
import org.javarosa.core.util.MultiInputStream;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapListPoly;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public class MultiMessagePayload implements IDataPayload {
	/** IDataPayload **/
	Vector payloads = new Vector();
	
	/**
	 * Note: Only useful for serialization.
	 */
	public MultiMessagePayload() {
		//ONLY FOR SERIALIZATION
	}
	
	/**
	 * Adds a payload that should be sent as part of this
	 * payload.
	 * @param payload A payload that will be transmitted
	 * after all previously added payloads.
	 */
	public void addPayload(IDataPayload payload) {
		payloads.addElement(payload);
	}
	
	/**
	 *  @return A vector object containing each IDataPayload in this payload.
	 */
	public Vector getPayloads() {
		return payloads;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadStream()
	 */
	public InputStream getPayloadStream() {
		MultiInputStream bigStream = new MultiInputStream();
		Enumeration en = payloads.elements();
		while(en.hasMoreElements()) {
			IDataPayload payload = (IDataPayload)en.nextElement();
			bigStream.addStream(payload.getPayloadStream());
		}
		bigStream.prepare();
		return bigStream;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		//payloads = (Vector)ExtUtil.read(in, new ExtWrapList(new ExtWrapTagged()), pf);
		payloads = (Vector)ExtUtil.read(in, new ExtWrapListPoly(), pf);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		//ExtUtil.write(out, new ExtWrapList(payloads));
		ExtUtil.write(out, new ExtWrapListPoly(payloads));
	}
	
	public Object accept(IDataPayloadVisitor visitor) {
		return visitor.visit(this);
	}

	public String getPayloadId() {
		return null;
	}

	public int getPayloadType() {
		return IDataPayload.PAYLOAD_TYPE_MULTI;
	}

	public long getLength() {
		int len = 0;
		Enumeration en = payloads.elements();
		while(en.hasMoreElements()) {
			IDataPayload payload = (IDataPayload)en.nextElement();
			len += payload.getLength();
		}
		return len;
	}
}
