package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.services.transport.impl.QueuingThread;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStore;

/**
 * The TransportService is generic. Its capabilities are extended by defining new
 * kinds of Transport.
 * 
 * To define a new kind of transport, it is necessary to implement two interfaces:
 * <ol>
 * <li>TransportMessage
 * <li>Transporter - an object with the ability to send one of the new kinds of message
 * </ol>
 * 
 * A TransportMessage must be able to
 * create an appropriate Transporter (via the <code>createTransporter()</code>
 * method) whose constructor takes the message itself.
 * 
 * The result is an intuitive programmer interface which involves the following steps
 * alone:
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
	 * 
	 * Send a message in a thread, using the default number of tries and the
	 * default pause between tries
	 * 
	 * Sending a message happens like this:
	 * 
	 * <ol>
	 * <li>The message creates an appropriate Transporter (which contains the
	 * message)
	 * <li>The message is given a QueuingDeadline, equal to the maximum time it
	 * can spend in a QueuingThread
	 * <li>The message is persisted in the Message Store
	 * <li>A QueuingThread is started, which tries and retries the Transporter's
	 * send method until either the specified number of tries are exhausted, or
	 * the message is successfully sent
	 * <li>The QueuingThread is returned
	 * </ol>
	 * 
	 * @param message
	 * @return Thread used to try to send message
	 * @throws IOException
	 */
	public QueuingThread send(TransportMessage message)
			throws TransportException {
		return send(message, QueuingThread.DEFAULT_TRIES,
				QueuingThread.DEFAULT_DELAY);
	}

	/**
	 * 
	 * Send a message, specifying a number of tries and the pause between the
	 * tries (in seconds)
	 * 
	 * 
	 * @param message
	 * @param tries
	 * @param delay
	 * @return
	 * @throws IOException
	 */
	public QueuingThread send(TransportMessage message, int tries, int delay)
			throws TransportException {

		// create the appropriate transporter
		Transporter transporter = message.createTransporter();

		// create a sender thread
		QueuingThread thread = new QueuingThread(transporter, MESSAGE_STORE,
				tries, delay);

		// record the deadline for the queuing phase in the message
		message.setQueuingDeadline(getQueuingDeadline(thread.getTries(), thread
				.getDelay()));

		// persist the message in the queue
		MESSAGE_STORE.enqueue(message);

		// start the queuing phase
		thread.start();

		// return the sender thread in case
		// an application wants to permit the user to cancel it
		return thread;
	}

	/**
	 * 
	 * Compute the lifetime of a queuing thread with the given parameters 
	 * 
	 * @param tries
	 * @param delay
	 * @return
	 */
	private long getQueuingDeadline(int tries, int delay) {
		long deadline = (tries * delay * 1000);
		long now = new Date().getTime();
		return (deadline + now);
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
	public void sendCached() throws TransportException {
		Vector messages = getCachedMessages();
		for (int i = 0; i < messages.size(); i++) {
			TransportMessage message = (TransportMessage) messages.elementAt(i);
			try {
				sendCachedMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
				message.setFailureReason(e.getMessage());

				MESSAGE_STORE.updateMessage(message);

			}
		}
	}

	/**
	 * 
	 * 
	 * Try to send one cached message, by creating a QueuingThread which will
	 * try just once
	 * 
	 * 
	 * @param message
	 * @throws IOException
	 */
	private void sendCachedMessage(TransportMessage message) throws IOException {
		// create the appropriate transporter
		Transporter transporter = message.createTransporter();
		// create a sender thread and start it
		new Thread(new QueuingThread(transporter, MESSAGE_STORE, 1, 0)).start();
	}

	/**
	 * @return
	 */
	public Vector getCachedMessages() {
		return MESSAGE_STORE.getCachedMessages();
	}

	/**
	 * @return
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
