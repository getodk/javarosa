package org.javarosa.communication.http;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.IDataPayloadVisitor;
import org.javarosa.core.services.transport.TransportMethod;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapMap;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A stream representing an HTTP header.
 * 
 * @author Clayton Sims
 * @date Dec 18, 2008 
 *
 */
public class HttpTransportHeader implements IDataPayload {

	/** String -> String **/
	Hashtable headers = new Hashtable();
	
	public HttpTransportHeader() {
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.services.transport.TransportHeaderStream#getTransportType()
	 */
	public int getTransportType() {
		return TransportMethod.HTTP_GCF;
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		
		headers = (Hashtable)ExtUtil.read(in, new ExtWrapMap(String.class, String.class));
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapMap(headers));
	}
	public Object accept(IDataPayloadVisitor visitor) {
		return null;
	}
	
	public String getPayloadId() {
		return null;
	}
	public InputStream getPayloadStream() {
		String header = "";
		Enumeration en = headers.keys();
		while(en.hasMoreElements()) {
			String key = (String)en.nextElement();
			String value = (String)headers.get(key);
			header += key + value + "\n";
		}
		return new ByteArrayInputStream(header.getBytes());
	}
	
	public int getPayloadType() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.services.transport.IDataPayload#getLength()
	 */
	public int getLength() {
		//Unimplemented for now
		return -1;
	}
}
