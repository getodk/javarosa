package org.javarosa.communication.http;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class HttpTransportDestination implements ITransportDestination {
	private String URL;
	
	/**
	 * NOTE: SHOULD ONLY BE USED FOR DESERIALIZATION!
	 */
	public HttpTransportDestination() { 
		
	}
	public HttpTransportDestination(String URL) {
		this.URL = URL;
	}
	
	public String getURL() {
		return URL;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		URL = in.readUTF();
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(URL);
	}
}
