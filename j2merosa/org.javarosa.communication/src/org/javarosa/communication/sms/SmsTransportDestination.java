//TODO: Add verification of SMS address

package org.javarosa.communication.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class SmsTransportDestination implements ITransportDestination{

	private String smsAddress;
	
	public SmsTransportDestination() {
		super();
	}
	
	public SmsTransportDestination(String smsAddress) {
		this.smsAddress = smsAddress;
	}
	
	public String getSmsAddress() {
		return smsAddress;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		smsAddress = in.readUTF();
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(smsAddress);
	}
	
	
}
