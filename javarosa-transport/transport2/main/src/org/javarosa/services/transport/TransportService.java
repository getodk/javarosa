package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Vector;

/**
 * The TransportService is generic and should not need to be changed when adding
 * new kinds of transport
 * 
 * To add a new kind of transport, it is necessary to define (1) A new kind of
 * TransportMessage (2) A new kind of Transporter - an object with the ability
 * to send one of the new kinds of message
 * 
 * The TransportMessage interface is such that each TransportMessage is able to
 * create an appropriate Transporter (via the <code>getTransporter()</code>
 * method) able to send it.
 * 
 * This is not ideal. To separate these would seem to make sense. Then we'd
 * have:
 * 
 * TransportMessage m = new SomeTransportMessage() Transporter tr = new
 * SomeTransporter(m); new TransportService().send(tr);
 * 
 * The most intuitive programmer interface however involves the following steps
 * alone: - create a Message - send the Message
 * 
 * TransportMessage m = new SomeTransportMessage() new
 * TransportService().send(m);
 * 
 * Requiring a Message to be able to create a Transporter is considered a price
 * worth paying.
 */
public class TransportService {

	/**
	 * 
	 * The transport service has a queue, via which messages to be sent are
	 * persisted
	 * 
	 */
	private static TransportQueue queue = new TransportQueue(false);

	/**
	 * 
	 * The basic purpose of the transport service - to send TransportMessages
	 * 
	 * (1) The TransportMessage is persisted in the TransportQueue and given a
	 * unique id (2) The message is asked for an appropriate Transporter (a
	 * Transporter actually does the communication) (3) A SenderThread is
	 * started, which tries and retries the Transporter (4) The unique id of the
	 * message is returned
	 * 
	 * 
	 * @param message
	 * @return
	 * @throws IOException
	 */
	public String send(TransportMessage message) throws IOException {
		// persist the message in the queue
		String id = queue.enqueue(message);
		// create the appropriate transporter
		Transporter transporter = message.getTransporter();
		// create a sender thread and start it
		SenderThread thread = new SenderThread(transporter, queue);
		thread.start();
		// return the queue-unique id of the message to be sent
		return id;

	}

	private void sendCachedMessage(TransportMessage message) throws IOException {
		// create the appropriate transporter
		Transporter transporter = message.getTransporter();
		// create a sender thread and start it
		SenderThread thread = new SenderThread(transporter, queue);
		thread.start();
	}

	public void sendCached() {
		Vector messages = getCachedMessages();
		for (int i = 0; i < messages.size(); i++) {
			TransportMessage message = (TransportMessage) messages.elementAt(i);
			try {
				sendCachedMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
				message.setFailureReason(e.getMessage());
			}
		}
	}

	/**
	 * @return
	 */
	public Vector getCachedMessages() {
		return queue.getCachedMessages();
	}

	/**
	 * @return
	 */
	public int getTransportQueueSize() {
		return queue.getTransportQueueSize();
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
		return queue.findMessage(id);
	}

}
