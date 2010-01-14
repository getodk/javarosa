package org.javarosa.services.transport.senders;

import java.util.Vector;

import org.javarosa.services.transport.TransportCache;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportException;

/**
 * 
 * A SenderThread takes a Transporter object and calls its send method
 * repeatedly until it succeeds, over a given number of tries, with a given
 * delay between each try
 * 
 */
public abstract class SenderThread extends Thread {

	/**
	 * 
	 */
	public static final int DEFAULT_TRIES = 2;
	/**
	 * 
	 */
	public static final int DEFAULT_DELAY = 30;

	public Vector listeners = new Vector();

	/**
	 * the Transporter has the TransportMessage, and knows how to send it
	 */
	protected Transporter transporter;

	/**
	 * A reference to the TransportMessageStore is needed so that successfully
	 * sent messages can be removed
	 */
	protected TransportCache messageStore;

	/**
	 * Number of times to try
	 */
	protected int tries = DEFAULT_TRIES;
	/**
	 * Length of pauses between tries (in seconds)
	 */
	protected int delay = DEFAULT_DELAY;

	/**
	 * Variable used to count down remaining tries
	 */
	protected int triesRemaining;

	/**
	 * @param transporter
	 *            The transporter object which does the sending
	 * @param queue
	 *            The transport queue is passed to the sender thread so that
	 *            messages which are successfully sent can be removed from the
	 *            queue
	 */
	protected SenderThread(Transporter transporter, TransportCache queue) {
		this.transporter = transporter;
		this.messageStore = queue;
		this.tries = DEFAULT_TRIES;
		this.delay = DEFAULT_DELAY;
	}

	/**
	 * @param transporter
	 *            The transporter object which does the sending
	 * @param queue
	 *            The transport queue is passed to the sender thread so that
	 *            messages which are successfully sent can be removed from the
	 *            queue
	 */
	protected SenderThread(Transporter transporter, TransportCache queue,
			int tries, int delay) {
		this.transporter = transporter;
		this.messageStore = queue;
		this.tries = tries;
		this.delay = delay;
	}

	protected abstract void send();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		send();
	}

	/**
	 * 
	 * Asks the Transporter to send the message, and then takes appropriate
	 * action, depending on whether the attempt was successful or not
	 * 
	 * @return The message being sent (with updated status)
	 */
	protected TransportMessage attemptToSend() throws TransportException {

		notifyChange(this.transporter.getMessage(), "Attempts left: "
				+ this.triesRemaining);
		TransportMessage message = this.transporter.send();
		if (message.isSuccess()) {

			onSuccess(message);
			notifyStatusChange(message);
		} else {
			onFailure();
		}
		return message;
	}

	/**
	 * If the message has been successfully sent, and had been cached, it should be removed from the
	 * TransportQueue
	 * 
	 * @param message
	 */
	protected void onSuccess(TransportMessage message)
			throws TransportException {
		if (message.isCacheable()) {
			// remove from queue
			this.messageStore.decache(message);
		}
	}

	/**
	 * If the message couldn't be sent, then reduce the number of available
	 * tries and pause the thread before making the next try
	 */
	protected void onFailure() {
		// used another attempt
		triesRemaining--;
		try {
			// pause before trying again
			sleep((long) delay * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	public int getTries() {
		return tries;
	}

	/**
	 * @return
	 */
	public int getDelay() {
		return delay;
	}

	/**
	 * @param listener
	 */
	public void addListener(TransportListener listener) {
		this.listeners.addElement(listener);
	}

	/**
	 * @param message
	 * @param remark
	 */
	public void notifyChange(TransportMessage message, String remark) {
		for (int i = 0; i < this.listeners.size(); i++) {
			((TransportListener) this.listeners.elementAt(i)).onChange(message,
					remark);
		}
	}

	/**
	 * @param message
	 */
	public void notifyStatusChange(TransportMessage message) {
		for (int i = 0; i < this.listeners.size(); i++) {
			((TransportListener) this.listeners.elementAt(i))
					.onStatusChange(message);
		}
	}

	/**
	 * 
	 */
	public void cancel() {
		this.interrupt();
	}

}
