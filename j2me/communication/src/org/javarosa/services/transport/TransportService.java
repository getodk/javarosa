package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.javarosa.core.services.Logger;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.TransportMessageStore;
import org.javarosa.services.transport.senders.SenderThread;
import org.javarosa.services.transport.senders.SimpleSenderThread;
import org.javarosa.services.transport.senders.TransporterSharingSender;

/**
 * The TransportService is generic. Its capabilities are extended by defining
 * new kinds of Transport.
 * 
 * To define a new kind of transport, it is necessary to implement two
 * interfaces:
 * <ol>
 * <li>TransportMessage - a new kind of message
 * <li>Transporter - an object with the ability to send one of the new messages
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
 * TransportService.send(m);
 * </code>
 * 
 */
public class TransportService {

	/**
	 * 
	 * The TransportService has a cache, in which all messages to be sent are
	 * persisted immediately
	 * 
	 */
	private static TransportMessageStore T_CACHE;
	
	private static TransporterSharingSender SENDER;
	
	private static final String CACHE_LOCK="CACHE_LOCK";
	
	private static TransportMessageStore CACHE() {
		synchronized(CACHE_LOCK) { 
			if(T_CACHE == null) {
				T_CACHE = new TransportMessageStore();
			}
			return T_CACHE;
		}
	}
	
	public static synchronized void init() {
		CACHE();
		SENDER  = new TransporterSharingSender();
	}

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
	 * <li>The message is given a SenderDeadline, equal to the maximum time it
	 * can spend in a SenderThread
	 * <li>The message is persisted in the cache
	 * <li>A SenderThread is started, which tries and retries the Transporter's
	 * send method until either the specified number of tries are exhausted, or
	 * the message is successfully sent
	 * <li>The SenderThread is returned
	 * </ol>
	 * 
	 * @param message
	 * @return Thread used to try to send message
	 * @throws IOException
	 */
	public static SenderThread send(TransportMessage message)
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
	public static SenderThread send(TransportMessage message, int tries,
			int delay) throws TransportException {

		// create the appropriate transporter
		Transporter transporter = message.createTransporter();

		// create a sender thread
		SenderThread thread = new SimpleSenderThread(transporter, CACHE(), tries,
				delay);

		// if the message should be stored and never lost
		if (message.isCacheable()) {

			// record the deadline for the sending thread phase in the message
			message.setSendingThreadDeadline(getSendingThreadDeadline(thread
					.getTries(), thread.getDelay()));

			synchronized (CACHE()) {
				// persist the message
				CACHE().cache(message);
				
				if (tries == 0) {
					Logger.log("send", "msg cached " + message.getTag() + "; " + CACHE().getCachedMessagesCount() + " in queue");
				}
			}
		} else {
			message.setStatus(TransportMessageStatus.QUEUED);
		}

		// start the sender thread
		Logger.log("send", "start " + message.getTag());
		thread.start();

		// return the sender thread in case
		// an application wants to permit the user to cancel it
		return thread;
	}

	/**
	 * @param message
	 * @return
	 * @throws TransportException
	 */
	public static TransportMessage sendBlocking(TransportMessage message)
			throws TransportException {

		// if the message should be saved in case of sending failure
		if (message.isCacheable()) {
			// persist the message
			CACHE().cache(message);
		}
		// create the appropriate transporter
		Transporter transporter = message.createTransporter();

		transporter.setMessage(message);
		// and get the transporter to try to send the message
		transporter.send();

		// if the message had been cached..
		if (message.isCacheable()) {

			if (message.getStatus() == TransportMessageStatus.SENT) {
				// if it was sent successfully, then remove it from cache
				CACHE().decache(message);
			} else {
				// otherwise, set the status to cached
				message.setStatus(TransportMessageStatus.CACHED);
				CACHE().updateMessage(message);
			}
		}
		return message;
	}

	/**
	 * 
	 * Any messages which aren't successfully sent in SendingThreads are then
	 * "Cached".
	 * 
	 * Applications can activate new attempts to send the CachedMessages via
	 * this sendCached method
	 * 
	 * 
	 */
	public static void sendCached(TransportListener listener)
			throws TransportException {
		if(SENDER == null) {
			//This is very bad, and the service should have been initialized
			SENDER  = new TransporterSharingSender();
		}

		//We get into a lot of trouble with synchronicity if we just let all kinds of
		//senders start going at once, so we'll just use the one and queue up 
		//sendCached attempts.
		synchronized(SENDER) {
			// get all the cached messages
			Vector messages = getCachedMessages();
	
			if (messages.size() > 0) {
	
				// create one appropriate transporter (to share a connection, for
				// example)
				TransportMessage m = (TransportMessage) messages.elementAt(0);
	
				Transporter transporter = m.createTransporter();
				// get a bulk sender to use the transporter to send all messages
				SENDER.init(transporter, messages, CACHE(), listener);
	
				SENDER.send();
	
			}
		}
	}

	/**
	 * 
	 * Compute the lifetime of a sending thread with the given parameters and
	 * returns the time in the future at which this duration would have elapsed
	 * 
	 * @param tries
	 * @param delay
	 * @return
	 */
	private static long getSendingThreadDeadline(int tries, int delay) {
		long duration = (new Long(tries).longValue()
				* new Long(delay).longValue() * 1000);
		long now = new Date().getTime();
		return (now + duration);
	}

	/**
	 * @return
	 */
	public static Vector getCachedMessages() {
		return CACHE().getCachedMessages();
	}

	/**
	 * @return
	 */
	public static int getCachedMessagesSize() {
		return CACHE().getCachedMessagesCount();
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
	public static TransportMessage retrieve(String id) {
		return CACHE().findMessage(id);
	}
	
	public static void halt() {
		if(SENDER != null) {
			SENDER.halt();
		}
	}
}
