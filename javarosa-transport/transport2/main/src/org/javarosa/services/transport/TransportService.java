package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

/**
 * The TransportService is generic and should not need to be changed when adding
 * new kinds of transport
 * 
 * To add a new kind of transport, it is necessary to define 
 * <ol>
 * <li> A new kind of <b>TransportMessage</b>
 * <li> A new kind of <b>Transporter</b> - an object with the ability to send one of the new kinds of message
 * </ol>
 * 
 * The TransportMessage interface is such that each TransportMessage is able to
 * create an appropriate Transporter (via the <code>getTransporter()</code>
 * method) able to send it.
 * 
 * This is not ideal. To separate these would seem to make sense. Then we'd
 * have:
 * 
 * <code>
 * TransportMessage m = new SomeTransportMessage() 
 * Transporter tr = new SomeTransporter(m); 
 * new TransportService().send(tr);
 * </code>
 * 
 * The most intuitive programmer interface however involves the following steps
 * alone: 
 * <ol>
 * <li>create a Message
 * <li>send the Message
 * </ol>
 * 
 * <code>
 * TransportMessage m = new SomeTransportMessage() 
 * new TransportService().send(m);
 * </code>
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
	private static TransportMessageStore messageStore = new TransportMessageStore(false);

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
	 * @return Thread used to try to send message
	 * @throws IOException
	 */
	public QueuingThread send(TransportMessage message) throws IOException {
		return send(message,QueuingThread.DEFAULT_TRIES,QueuingThread.DEFAULT_DELAY);
	}
	public QueuingThread send(TransportMessage message,int tries,int delay) throws IOException {

		// create the appropriate transporter
		Transporter transporter = message.getTransporter();
		// create a sender thread 
		QueuingThread thread = new QueuingThread(transporter, messageStore);
		
		// record the deadline for the queuing phase in the message
		message.setQueuingDeadline(getQueuingDeadline(thread.getTries(), thread
				.getDelay()));
		
		// persist the message in the queue
		messageStore.enqueue(message);

		// start the queuing phase
		thread.start();
		
		// return the sender thread
		return thread;
	}

	private Date getQueuingDeadline(int tries, int delay) {
		long deadline = (tries * delay * 1000);
		long now = new Date().getTime();
		Date queuingDeadline = new Date(deadline + now);
		return queuingDeadline;
	}

	/**
	 * 
	 * Any messages which aren't successfully sent in QueuingThreads are then
	 * "Cached".
	 * 
	 * Applications can activate new attempts to send the CachedMessages via
	 * this sendCached method
	 * 
	 * 
	 */
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
	 * 
	 * 
	 * Try to send one cached message
	 * 
	 * 
	 * @param message
	 * @throws IOException
	 */
	private void sendCachedMessage(TransportMessage message) throws IOException {
		// create the appropriate transporter
		Transporter transporter = message.getTransporter();
		// create a sender thread and start it
		new QueuingThread(transporter, messageStore).start();
	}

	/**
	 * @return
	 */
	public Vector getCachedMessages() {
		return messageStore.getCachedMessages();
	}

	/**
	 * @return
	 */
	public int getTransportQueueSize() {
		return messageStore.getTransportQueueSize();
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
		return messageStore.findMessage(id);
	}

}
