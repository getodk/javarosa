package org.javarosa.communication.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class FileTransportDestination implements ITransportDestination {

	private String URI;
	
	/**
	 * NOTE: SHOULD ONLY BE USED FOR DESERIALIZATION!
	 */
	public FileTransportDestination() {
	}
	
	public FileTransportDestination(String URI) {
		this.URI = URI;
	}
	
	public String getURI() {
		return URI;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		URI = in.readUTF();
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(URI);
	}
}
