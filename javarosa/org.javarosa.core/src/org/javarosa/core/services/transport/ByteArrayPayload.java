/**
 * 
 */
package org.javarosa.core.services.transport;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.util.externalizable.DeserializationException;
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
		//ONLY FOR SERIALIZATION
	}
	
	public ByteArrayPayload(byte[] payload, String id, int type) {
		this.payload = payload;
		this.id = id;
		this.type = type;
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
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeInt(payload.length);
		if(payload.length > 0) {
			out.write(payload);
		}
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
}
