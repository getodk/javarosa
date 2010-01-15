package org.javarosa.services.transport.impl.binarysms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

/**
 * SMS message object
 * 
 * Since the message to be sent may require to be partitioned into more than one
 * SMS payloads, the content of the SMSTransportMessage is a Vector of Strings
 * (in the simplest case, vector size = 1)
 * 
 * 
 */
public class BinarySMSTransportMessage extends BasicTransportMessage {
	
	byte[] content;

	/**
	 * 
	 */
	private String destinationURL;

	/**
	 * FOR DESERIALIZATION ONLY!
	 */
	public BinarySMSTransportMessage() {
		//ONLY FOR DESERIALIZING
	}
	/**
	 * @param str
	 * @param destinationURL
	 */
	public BinarySMSTransportMessage(byte[] bytes, String destinationURL) {
		this.destinationURL = destinationURL;
		this.content = bytes;
	}

	public boolean isCacheable() {
		return true;
	}
	
	public Object getContent() {
		return content;
	}

	/**
	 * @return
	 */
	public String getDestinationURL() {
		return destinationURL;
	}

	/**
	 * @param destinationURL
	 */
	public void setDestinationURL(String destinationURL) {
		this.destinationURL = destinationURL;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.javarosa.services.transport.TransportMessage#createTransporter()
	 */
	public Transporter createTransporter() {
		return new BinarySMSTransporter(this);
	}

	public InputStream getContentStream() {
		return new ByteArrayInputStream((byte[]) getContent());
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		super.readExternal(in, pf);
		destinationURL = ExtUtil.readString(in);
		content = ExtUtil.readBytes(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.writeString(out, destinationURL);
		ExtUtil.writeBytes(out, content);
	}

}
