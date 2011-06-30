/**
 * 
 */
package org.javarosa.services.transport.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import org.javarosa.core.services.storage.IMetaData;
import org.javarosa.core.services.storage.WrappingStorageUtility.SerializationWrapper;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.TransportMessage;

/**
 * @author ctsims
 *
 */
public class TransportMessageSerializationWrapper implements SerializationWrapper, IMetaData {
	
	TransportMessage m;

	public Class baseType() {
		return TransportMessage.class;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.WrappingStorageUtility.SerializationWrapper#getData()
	 */
	public Externalizable getData() {
		return m;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.services.storage.WrappingStorageUtility.SerializationWrapper#setData(org.javarosa.core.util.externalizable.Externalizable)
	 */
	public void setData(Externalizable e) {
		m = (TransportMessage)e;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		m = (TransportMessage)ExtUtil.read(in, new ExtWrapTagged(),pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(m));
	}
	

	public Hashtable getMetaData() {
		Hashtable meta = new Hashtable();
		meta.put("cache-id", m.getCacheIdentifier());
		return meta;
	}

	public Object getMetaData(String fieldName) {
		if(fieldName.equals("cache-id")) {
			return m.getCacheIdentifier();
		}
		throw new IllegalArgumentException("No metadata field " + fieldName  + " for stored transport messages"); 
	}

	public String[] getMetaDataFields() {
		return new String[] { "cache-id" };
	}

}
