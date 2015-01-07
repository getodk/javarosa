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
package org.javarosa.core.model.instance.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IInstanceSerializingVisitor;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.transport.payload.IDataPayload;
import org.javarosa.core.services.transport.payload.IDataPayloadVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * The ModelReferencePayload essentially provides a wrapper functionality
 * around a ModelTree to allow it to be used as a payload, but only to
 * actually perform the various computationally expensive functions
 * of serialization when required.
 *
 * @author Clayton Sims
 * @date Apr 27, 2009
 *
 */
public class ModelReferencePayload implements IDataPayload {

	int recordId;
	IDataPayload payload;
	String destination = null;

	IInstanceSerializingVisitor serializer;

	//NOTE: Should only be used for serializaiton.
	public ModelReferencePayload() {

	}

	public ModelReferencePayload(int modelRecordId) {
		this.recordId = modelRecordId;
	}

	/**
	 * @param serializer the serializer to set
	 */
	public void setSerializer(IInstanceSerializingVisitor serializer) {
		this.serializer = serializer;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#accept(org.javarosa.core.services.transport.IDataPayloadVisitor)
	 */
	public <T> T accept(IDataPayloadVisitor<T> visitor) {
		memoize();
		return payload.accept(visitor);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getLength()
	 */
	public long getLength() {
		memoize();
		return payload.getLength();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadId()
	 */
	public String getPayloadId() {
		memoize();
		return payload.getPayloadId();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadStream()
	 */
	public InputStream getPayloadStream() throws IOException {
		memoize();
		return payload.getPayloadStream();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getPayloadType()
	 */
	public int getPayloadType() {
		memoize();
		return payload.getPayloadType();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		recordId = in.readInt();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(recordId);
	}

	private void memoize() {
		if(payload == null) {
			IStorageUtility<FormInstance> instances = (IStorageUtility<FormInstance>) StorageManager.getStorage(FormInstance.STORAGE_KEY);
			try {
				FormInstance tree = instances.read(recordId);
				payload = serializer.createSerializedPayload(tree);
			} catch (IOException e) {
				//Assertion, do not catch!
				e.printStackTrace();
				throw new RuntimeException("ModelReferencePayload failed to retrieve its model from rms [" + e.getMessage() + "]");
			}
		}
	}

	public int getTransportId() {
		return recordId;
	}

	public void setDestination(String destination){
		this.destination = destination;
	}

	public String getDestination() {
		return destination;
	}
}
