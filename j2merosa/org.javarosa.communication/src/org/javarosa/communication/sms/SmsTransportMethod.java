/**
 * Class:	SmsTransportMethod
 * @author 	vijayu
 * Desc:	This class represents a transport method using SMS and the procedures for
 * 			transmitting a TransportMessage using the SMS protocol.  The defining feature
 * 			of SMS is the character limit imposed for a given SMS message.  To account for
 * 			this, we use a SMSSplitTransportMessage to automatically split the message into
 * 			parts that we know will each fit into SMS messages with easy to parse metadata
 * 			for each message part.
 */

//TODO: Handle Incoming SMS Messages - right now, our thread only transmits

package org.javarosa.communication.sms;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

import org.javarosa.core.api.IActivity;
import org.javarosa.core.services.ITransportManager;
import org.javarosa.core.services.transport.ITransportDestination;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.services.transport.TransportMethod;

public class SmsTransportMethod implements TransportMethod {

	private TransportMessage message;
	//private ITransportManager manager;
	private IActivity destinationRetrievalActivity;
	private WorkerThread primaryWorker;
	private static final String name = "SMS";
	//private static final String SMSPort = "16498";	//TODO: Clarify - this was taken from previous CRS App
	
	public void transmit(TransportMessage message, ITransportManager manager) {
		this.message = message;
	//	this.manager = manager;
		primaryWorker = new WorkerThread();
		new Thread(primaryWorker).start();
	}
		
	public void closeConnections() {
		if(primaryWorker != null)
			primaryWorker.cleanStreams();
	}

	public ITransportDestination getDefaultDestination() {
		//FIXME: Figure out the SMS application of this...
		return new SmsTransportDestination("sms://+5566511");
	}


	public IActivity getDestinationRetrievalActivity() {
		return destinationRetrievalActivity;
	}

	public int getId() {
		return TransportMethod.SMS;
	}

	public String getName() {
		return name;
	}

	public void setDestinationRetrievalActivity(IActivity activity) {
		destinationRetrievalActivity = activity;
	}
	
	private class WorkerThread implements Runnable, MessageListener{
		private MessageConnection mconn;
		
		public void cleanStreams(){
			if (mconn != null) {
				try {
					mconn.close();
				} catch (IOException e) {
					System.err.println("IO Exception while closing SMS Message Connection");
					e.printStackTrace();
				}
			}	
		}

		public void run() {
			// Open an SMS Message connection to send the messages
			String destinationUrl = ((SmsTransportDestination)message.getDestination()).getSmsAddress();
			try {
				SMSSplitTransportMessage sp = new SMSSplitTransportMessage(message);
				sp.splitMessage();
				
				// Set destination URL from TransportMessage data
				mconn = (MessageConnection)Connector.open(destinationUrl);
				
				Enumeration e = sp.getMessageParts().elements();
				TextMessage tmsg;	
				
				while(e.hasMoreElements()) {
					tmsg = (TextMessage)mconn.newMessage(MessageConnection.TEXT_MESSAGE);
					tmsg.setAddress(destinationUrl);
					String payload = new String(((ByteArrayOutputStream)e.nextElement()).toByteArray());
					System.out.println("SMS Payload: " + payload);
					tmsg.setPayloadText(payload);
					mconn.send(tmsg);
				}
				//FIXME: Risk of memory leak with tmsg here??
				
				message.setStatus(TransportMessage.STATUS_DELIVERED);
				System.out.println("Status: " + message.getStatus());
				message.setChanged();
				message.notifyObservers(message.getReplyloadData());

			} catch (IOException e) {
				System.err.println("Error sending SMS message");
				e.printStackTrace();
			}
			
			
		}
		
		

		public void notifyIncomingMessage(MessageConnection arg0) {
			// TODO: Implement thread for receiving messages
		}
		
	}

}


