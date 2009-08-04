package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.services.transport.impl.QueuingThread;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStore;
import org.javarosa.services.transport.listeners.TransportListener;
import org.javarosa.services.transport.message.TransportMessage;

/**
 * The TransportService is generic. Its capabilities are extended by defining
 * new kinds of Transport.
 * 
 * To define a new kind of transport, it is necessary to implement two
 * interfaces:
 * <ol>
 * <li>TransportMessage
 * <li>Transporter - an object with the ability to send one of the new kinds of
 * message
 * </ol>
 * 
 * A TransportMessage must be able to create an appropriate Transporter (via the
 * <code>createTransporter()</code> method) whose constructor takes the message
 * itself.
 * 
 * The result is an intuitive programmer interface which involves the following
 * steps alone:
 * <ol>
 * <li>create a Message
 * <li>ask the TransportService to send the Message
 * </ol>
 * 
 * For example:
 * 
 * <code>
 * TransportMessage m = new SomeTransportMessage() 
 * new TransportService().send(m);
 * </code>
 * 
 */
public class TransportService {

	/**
	 * 
	 * The TransportService has a messageStore, in which all messages to be sent
	 * are persisted immediately
	 * 
	 */

	private static TransportMessageStore MESSAGE_STORE = new TransportMessageStore();
	
	/**
	 * Attempts to send the provided message, storing it inside of the cache until it
	 * has successfully been transported.  
	 * 
	 * @param message The message that is to be sent.
	 * @return The transporter which will be used to send the message. This object can
	 * be used to cancel the current send if necessary. 
	 * 
	 * @throws TransportException If there are problems enqueuing or dequeuing the message
	 * from the cache 
	 */
	public ITransporter send(TransportMessage message) throws TransportException{
		final TransportMessage tmessage = message;
		ITransporter transporter = message.createTransporter();
		MESSAGE_STORE.enqueue(message);
		
		message.addTransportListener(new TransportListener() {

			public void onFailure(TransportMessage msg, int failureType, String failureMessage) {
				//Don't do anything, we don't really need to
			}

			public void onSuccess(TransportMessage msg, byte[] response) {
				try {
					MESSAGE_STORE.dequeue(tmessage);
				} catch (TransportException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			public void onUpdate(TransportMessage msg, String message) {
				//Don't do anything, if the outside is listening, they'll do something
			}
			
		});
		
		transporter.send(message);
		return transporter;
	}
	
	/**
	 * Attempts to send the provided transport message and retrieve a response from
	 * the destination. If the Transport Service cannot successfully transport the message
	 * or elicit a response, a TransportException is thrown outlining the problems.
	 * 
	 * Note that the blocking form of transport does not queue the messages to be 
	 * resent later in the case of failure.
	 * 
	 * @param message The message which should be sent.
	 * @return A byte[] containing the data of the response from the server.
	 * @throws TransportException If there are any problems sending the message
	 * to the server, or retrieving a response.
	 */
	public byte[] sendBlocking(TransportMessage message) throws TransportException {
		ITransporter transporter = message.createTransporter();
		
		transporter.send(message);
		
		while(message.getStatus() == TransportListener.TRANSPORTING) {
			//Wait until the status has changed.
			
			//TODO: Configure this parameter to be reasonable.
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(message.getStatus() == TransportListener.SUCCESS) {
			return message.getResponse();
		} else {
			throw new TransportException("Problem while sending message. Failure was: " + new String(message.getResponse()));
		}
	}

	/**
	 * 
	 * @return a Vector<TransportMessage> of messages which have been queued in the past, but 
	 * never successfully sent off of the phone.
	 */
	public Vector getCachedMessages() {
		return MESSAGE_STORE.getCachedMessages();
	}

	/**
	 * @return The number of messages which are in the unsent cache.
	 */
	public int getCachedMessagesSize() {
		return MESSAGE_STORE.getCachedMessagesSize();
	}

	/**
	 * 
	 * A TransportMessage is assigned a uniqueId when persisted. Applications
	 * can access the message again via this method
	 * 
	 * @param id
	 *            The unique id assigned to the TransportMessage when it was
	 *            queued for sending
	 * @return The TransportMessage identified by the id (or null if no such
	 *         message was found)
	 */
	public TransportMessage retrieve(String id) {
		return MESSAGE_STORE.findMessage(id);
	}
}
