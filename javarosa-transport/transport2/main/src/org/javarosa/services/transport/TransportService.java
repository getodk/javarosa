package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Vector;


/**
 * The TransportService is generic and should not need to be changed 
 * when adding new kinds of transport
 * 
 * To add a new kind of transport, it is necessary to define
 * (1) A new kind of TransportMessage
 * (2) A new kind of Transporter - an object with the ability to send one of the new kinds of message
 * 
 *
 */
public class TransportService {

	/**
	 * 
	 * The transport service has a queue, via which
	 * messages to be sent are persisted
	 * 
	 */
	private static TransportQueue queue = new TransportQueue(false);

	/**
	 * 
	 * The basic purpose of the transport service - to send TransportMessages
	 * 
	 * (1) The TransportMessage is  persisted in the TransportQueue and given a unique id
	 * (2) The message is asked for an appropriate Transporter 
	 * (a Transporter actually does the communication)
	 * (3) A SenderThread is started, which tries and retries the Transporter
	 * (4) The unique id of the message is returned
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

	/**
	 * @return
	 */
	public Vector getTransportQueue() {
		return queue.getTransportQueue();
	}

	/**
	 * @return
	 */
	public int getTransportQueueSize() {
		return queue.getTransportQueueSize();
	}

	/**
	 * 
	 * A TransportMessage is assigned a uniqueId when persisted. Applications can
	 * access the message again via this method
	 * 
	 * @param id The unique id assigned to the TransportMessage when it was queued for sending
	 * @return The TransportMessage identified by the id (or null if no such message was found)
	 */
	public TransportMessage retrieve(String id) {
		return queue.findMessage(id);
	}

}
