/**
 * Class:	SMSSplitTransportMessage
 * @author vijayu
 * Desc:	This class is utilized by the SMS Transport layer and is reponsible for splitting
 * 			a TransportMessage payload into parts that will fit within the SMS size limits as
 * 			defined in SMSTransportProperties (usually 140 or 160 characters).  
 */

package org.javarosa.communication.sms;

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
		
		// Create a new vector to store the message strings
		if(messageParts == null)
			messageParts = new Vector();
		
		// Define the number of message parts we will need based on the size of
		// the message payload and the metadata
		int maxPayload = SmsTransportProperties.MAX_SMS_SIZE - metaData(0,0).length();
		/** int numParts = payload.length / maxPayload;
		if(payload.length % maxPayload == 0)
			numParts ++;
		
		for(int i = 0; i < numParts; i++) {
			byte[] part = new byte[SmsTransportProperties.MAX_SMS_SIZE];
			byte[] meta = metaData(i+1,numParts).getBytes();
			
			// Create a byte[] with metadata and partial payload data for each message part
			System.arraycopy(meta, 0, part, 0, meta.length);
			System.arraycopy(payload, i*maxPayload, part, meta.length, maxPayload);
		
			// Add the message part to the vector
			messageParts.addElement(part);
		} **/
	}
	
	/**
	 * Application specific method that generates metadata for this message part
	 * Format: "f=<FormID>&p=<CurrentMessagePartNo>,<TotalNumberOfMessages>$" (payload will follow here)
	 * @param currentMsg
	 * @param totalMsg
	 * @return
	 */
	private String metaData(int currentMsg, int totalMsg) {
		//TODO: Change the formID to something alphanumeric to save space
		return "f="+formID+"&p="+currentMsg+","+totalMsg+"$";
	}
	
	
	
}
