package org.javarosa.services.transport.threading;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.TransportMessageStore;

/**
 * 
 * A QueuingThread takes a Transporter object and calls its send method
 * repeatedly until it succeeds, over a given number of tries, with a given
 * delay between each try
 * 
 */
public class QueuingThread extends Thread {

	/**
	 * 
	 */
	public final static int DEFAULT_TRIES = 5;
	/**
	 * 
	 */
	public final static int DEFAULT_DELAY = 60;

	/**
	 * the Transporter has the TransportMessage, and knows how to send it
	 */
	private Transporter transporter;
	
	/**
	 * A reference to the TransportMessageStore is needed so that successfully
	 * sent messages can be removed
	 */
	private TransportMessageStore messageStore;

	/**
	 * Number of times to try
	 */
	private int tries = DEFAULT_TRIES;
	/**
	 * Length of pauses between tries (in seconds)
	 */
	private int delay = DEFAULT_DELAY;

	/**
	 * Variable used to count down remaining tries
	 */
	private int triesRemaining;

	/**
	 * @param transporter
	 *            The transporter object which does the sending
	 * @param queue
	 *            The transport queue is passed to the sender thread so that
	 *            messages which are successfully sent can be removed from the
	 *            queue
	 */
	public QueuingThread(Transporter transporter, TransportMessageStore queue) {
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
	public QueuingThread(Transporter transporter, TransportMessageStore queue,
			int tries, int delay) {
		this.transporter = transporter;
		this.messageStore = queue;
		this.tries = tries;
		this.delay = delay;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		TransportMessage message = this.transporter.getMessage();

		this.triesRemaining = this.tries;
		// try to send repeatedly for a given number of tries
		// or until the message has been successfully sent
		while ((this.triesRemaining > 0) && !message.isSuccess()) {
			try {
				message = attemptToSend();
			} catch (TransportException e) {
				e.printStackTrace();
				// can't log and throw this from within the API
				// and since this is in a thread, nothing to do
			}
		}

		// if the loop was executed merely because the tries have been
		// used up, then the message becomes cached, for sending
		// via the "Send Unsent" user function
		if (!message.isSuccess()) {
			message.setStatus(TransportMessageStatus.CACHED);
		}
		try {
			this.messageStore.updateMessage(message);
		} catch (Exception e) {
			e.printStackTrace();
			// if this update fails, the QUEUED status
			// isn't permanent (the message doesn't fall through
			// a gap) because we note the duration of the QueuingThread
			// and, should a message be found with the status QUEUED
			// and yet was created before (now-queuing thread duration)
			// then it is considered to have the CACHED status
		}
	}

	/**
	 * 
	 * Asks the Transporter to send the message, and then takes appropriate
	 * action, depending on whether the attempt was successful or not
	 * 
	 * @return The message being sent (with updated status)
	 */
	private TransportMessage attemptToSend() throws TransportException {
		System.out.println("Attempts left: " + this.triesRemaining);
		TransportMessage message = this.transporter.send();
		if (message.isSuccess()) {
			onSuccess(message);
		} else {
			onFailure();
		}
		return message;
	}

	/**
	 * If the message has been successfully sent, it should be removed from the
	 * TransportQueue
	 * 
	 * @param message
	 */
	private void onSuccess(TransportMessage message) throws TransportException {
		// remove from queue

		this.messageStore.dequeue(message);

	}

	/**
	 * If the message couldn't be sent, then reduce the number of available
	 * tries and pause the thread before making the next try
	 */
	private void onFailure() {
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

}
