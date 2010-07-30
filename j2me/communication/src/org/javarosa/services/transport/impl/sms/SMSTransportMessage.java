package org.javarosa.services.transport.impl.sms;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.BasicTransportMessage;

/**
 * 
 * Wireless Messaging API (JSR-120 or WMA)
 * 
 * 
 * SMS message object
 * 
 * Since the message to be sent may require to be partitioned into more than one
 * SMS payloads, the content of the SMSTransportMessage is a Vector of Strings
 * (in the simplest case, vector size = 1)
 * 
 * 
 */
public class SMSTransportMessage extends BasicTransportMessage {

	/**
	 * SMS messages can be no longer than 140 characters in length
	 */
	public final static int MAX_SMS_LENGTH = 140;

	/**
	 * 
	 */
	private String destinationURL;
	
	private Vector content;

	/**
	 * NOTE: DO NOT USE. ONLY FOR SERIALIZATION
	 */
	public SMSTransportMessage() {
		//ONLY FOR SERIALIZATION
	}
	
	/**
	 * @param str
	 * @param destinationURL
	 */
	public SMSTransportMessage(String str, String destinationURL) {
		this.destinationURL = destinationURL;
		content = splitSMS(str);
	}

	public boolean isCacheable() {
		return false;
	}
	public boolean isShareTransporter(){
		return false;
	}
	

	public Object getContent() {
		return content;
	}

	/**
	 * 
	 * SMS can be of maximum 140 characters in length.
	 * 
	 * If the message to be sent is greater, it is partitioned.
	 * 
	 * @param str
	 * @return Vector of strings to be sent as separate messages
	 */
	private Vector splitSMS(String str) {
		String message = str;
		Vector v = new Vector();

		// if message is too long split it
		while (message.length() > MAX_SMS_LENGTH) {
			String part = message.substring(0, MAX_SMS_LENGTH);
			v.addElement(part);
			message = message.substring(MAX_SMS_LENGTH + 1);
		}

		// whatever remaining of the message after
		// chopping out 140 character length chunks
		// must also be added
		if (message.length() > 0)
			v.addElement(message);
		return v;
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
		return new SMSTransporter(this);
	}
	
	// TODO: content is a string Vector, so this is wrong
	public InputStream getContentStream(){
		return new ByteArrayInputStream((byte[])getContent());
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
		content = (Vector)ExtUtil.read(in, new ExtWrapList(String.class));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
		ExtUtil.write(out, new ExtWrapList(content));
	}

}
