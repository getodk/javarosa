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
package org.javarosa.core.services.transport.payload;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A ByteArrayPayload is a simple payload consisting of a
 * byte array.
 *
 * @author Clayton Sims
 * @date Dec 18, 2008
 *
 */
public class ByteArrayPayload implements IDataPayload {
	byte[] payload;

	String id;

	int type;

	/**
	 * Note: Only useful for serialization.
	 */
	public ByteArrayPayload() {
	}

	/**
	 *
	 * @param payload The byte array for this payload.
	 * @param id An optional id identifying the payload
	 * @param type The type of data for this byte array
	 */
	public ByteArrayPayload(byte[] payload, String id, int type) {
		this.payload = payload;
		this.id = id;
		this.type = type;
	}

	/**
	 *
	 * @param payload The byte array for this payload.
	 */
	public ByteArrayPayload(byte[] payload) {
		this.payload = payload;
		this.id = null;
		this.type = IDataPayload.PAYLOAD_TYPE_XML;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadStream()
	 */
	public InputStream getPayloadStream() {

		return new ByteArrayInputStream(payload);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		int length = in.readInt();
		if(length > 0) {
			this.payload = new byte[length];
			in.read(this.payload);
		}
		id = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(payload.length);
		if(payload.length > 0) {
			out.write(payload);
		}
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(id));
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#accept(org.javarosa.core.services.transport.IDataPayloadVisitor)
	 */
	public <T> T accept(IDataPayloadVisitor<T> visitor) {
		return visitor.visit(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadId()
	 */
	public String getPayloadId() {
		return id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadType()
	 */
	public int getPayloadType() {
		return type;
	}

	public long getLength() {
		return payload.length;
	}

	public int getTransportId() {
		//TODO: Most messages can include this data
		return -1;
	}
}
