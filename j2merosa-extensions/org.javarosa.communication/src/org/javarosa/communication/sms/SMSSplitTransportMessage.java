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
 * Class:	SMSSplitTransportMessage
 * @author vijayu
 * Desc:	This class is utilized by the SMS Transport layer and is reponsible for splitting
 * 			a TransportMessage payload into parts that will fit within the SMS size limits as
 * 			defined in SMSTransportProperties (usually 140 or 160 characters).  
 */

package org.javarosa.communication.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.javarosa.core.services.transport.IDataPayload;
import org.javarosa.core.services.transport.TransportMessage;

public class SMSSplitTransportMessage implements SplitTransportMessage {

	private Vector messageParts;
	private int formID;
	private IDataPayload payload;
	
	
	public SMSSplitTransportMessage(TransportMessage tm) {
		this.formID = tm.getRecordId();
		this.payload = tm.getPayloadData();
	}
	
	public void setPayloadData(IDataPayload payload) {
		this.payload = payload;
	}
	
	public Vector getMessageParts() {
		return messageParts;
	}
	
	public int getFormID() {
		return formID;
	}
	
	public void splitMessage() {
		System.out.println("SPLITTING MESSAGE");
		// Create a new vector to store the message strings
		if(messageParts == null)
			messageParts = new Vector();
		
		// Define the number of message parts we will need based on the size of
		// the message payload and the metadata
		//FIXME: This only handles messages of <10 parts!
		long maxPayload = SmsTransportProperties.MAX_SMS_SIZE - metaData(0,0).length();
		System.out.println("Max Payload: "+maxPayload);
		long numParts = payload.getLength() / maxPayload;
		System.out.println("Num Parts: "+numParts+1);
		if(payload.getLength() % maxPayload > 0)
			numParts++;
		try {
			InputStream istream = payload.getPayloadStream();
			long currentPart = 0;
			
			while(currentPart < numParts) {
				ByteArrayOutputStream ostream = new ByteArrayOutputStream();
				byte[] meta = metaData(currentPart+1,numParts).getBytes();
				int byteCounter = 0;
				ostream.write(meta);
				
				byte nextByte = (byte) istream.read();
				
				while(nextByte != -1 && byteCounter < maxPayload) {
					ostream.write(nextByte);
					byteCounter++;
					nextByte = (byte) istream.read();
				}
				
				messageParts.addElement(ostream);
				currentPart++;
			}
			
		}
		catch(IOException e) {
			System.err.println("IO Exception while splitting payload");
		}
			
	}
	
	/**
	 * Application specific method that generates metadata for this message part
	 * Format: "f=<FormID>&p=<CurrentMessagePartNo>,<TotalNumberOfMessages>$" (payload will follow here)
	 * @param currentMsg
	 * @param totalMsg
	 * @return
	 */
	private String metaData(long currentMsg, long totalMsg) {
		//TODO: Change the formID to something alphanumeric to save space
		return "f="+formID+"&p="+currentMsg+","+totalMsg+"$";
	}
	
	
	
}
