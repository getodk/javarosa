package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStore;
import org.javarosa.services.transport.senders.BulkSenderThread;
import org.javarosa.services.transport.senders.SenderThread;
import org.javarosa.services.transport.senders.SimpleSenderThread;

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
	public SenderThread send(TransportMessage message)
			throws TransportException {
		return send(message, SenderThread.DEFAULT_TRIES,
				SenderThread.DEFAULT_DELAY);
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
	public SenderThread send(TransportMessage message, int tries, int delay)
			throws TransportException {

		// create the appropriate transporter
		Transporter transporter = message.createTransporter();

		// create a sender thread
		SenderThread thread = new SimpleSenderThread(transporter,
				MESSAGE_STORE, tries, delay);

		// record the deadline for the queuing phase in the message
		message.setQueuingDeadline(getQueuingDeadline(thread.getTries(), thread
				.getDelay()));

		if (message.isCacheable()) {
			// persist the message
			MESSAGE_STORE.cache(message);
		}

		// start the queuing phase
		thread.start();

		// return the sender thread in case
		// an application wants to permit the user to cancel it
		return thread;
	}

	public TransportMessage sendBlocking(TransportMessage message) {
		// create the appropriate transporter
		Transporter transporter = message.createTransporter();

		transporter.setMessage(message);

		transporter.send();
		return message;
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
	public SenderThread sendCached() throws TransportException {
		Vector messages = getCachedMessages();
		if (messages.size() > 0) {
			// create an appropriate transporter
			TransportMessage m = (TransportMessage) messages.elementAt(0);
			Transporter transporter = m.createTransporter();
			BulkSenderThread thread = new BulkSenderThread(transporter,
					messages, MESSAGE_STORE, 1, 0);
			thread.start();
			return thread;

		}
		throw new TransportException("No cached messages to send");
	}

	/**
	 * 
	 * Compute the lifetime of a queuing thread with the given parameters
	 * 
	 * @param tries
	 * @param delay
	 * @return
	 */
	private static long getQueuingDeadline(int tries, int delay) {
		long duration = (new Long(tries).longValue()
				* new Long(delay).longValue() * 1000);
		long now = new Date().getTime();
		return (now + duration);
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
