package org.javarosa.communication.file;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;

import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class FileTransportDestination implements ITransportDestination {

	private String baseURI;
	
	/**
	 * NOTE: SHOULD ONLY BE USED FOR DESERIALIZATION!
	 */
	public FileTransportDestination() {
	}
	
	public FileTransportDestination(String URI) {
		this.baseURI = URI;
	}
	
	public String getURI() {
		return baseURI + generateUniqueName();
	}
	
	private String generateUniqueName() {
		String dateString = DateUtils.formatDateTime(new Date(), DateUtils.FORMAT_TIMESTAMP_SUFFIX);
		return dateString + ".xml";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		baseURI = in.readUTF();
		
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(baseURI);
	}
}
