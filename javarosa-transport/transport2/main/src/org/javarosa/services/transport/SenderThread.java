package org.javarosa.services.transport;

import java.io.IOException;


/**
 * 
 * A SenderThread takes a Transporter object and calls its send method
 * repeatedly until it succeeds, over a given number of tries, with a given
 * delay between each try
 *
 */
public class SenderThread extends Thread {

	private Transporter transporter;
	private TransportQueue queue;

	public static int TRIES = 5;
	public static int DELAY = 60;
	
	private int triesRemaining=TRIES;

	/**
	 * @param transporter The transporter object which does the sending
	 * @param queue The transport queue is passed to the sender thread so that messages which are successfully sent can be removed from the queue
	 */
	public SenderThread(Transporter transporter, TransportQueue queue) {
		this.transporter = transporter;
		this.queue = queue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		TransportMessage message = this.transporter.getMessage();
		// try to send repeatedly for a given number of tries
		// or until the message has been successfully sent
		while ((triesRemaining > 0) && !message.isSuccess()) {
			message = attemptToSend();
		}
		
		// if the loop was executed merely because the tries have been
		// used up, then the message becomes cached, for sending
		// via the "Send Unsent" user function
		if(!message.isSuccess()){
			message.setStatus(MessageStatus.CACHED);
		}
	}

	/**
	 * 
	 * Asks the Transporter to send the message, and then takes
	 * appropriate action, depending on whether the attempt was successful or not
	 * 
	 * @return The message being sent (with updated status)
	 */
	private TransportMessage attemptToSend() {
		TransportMessage message = this.transporter.send();
		if (message.isSuccess()) {
			onSuccess(message);
		} else {
			onFailure();
		}
		return message;
	}

	/**
	 * If the message has been successfully sent, it should be removed from the TransportQueue
	 * 
	 * @param message
	 */
	private void onSuccess(TransportMessage message) {
		// remove from queue
		try {
			this.queue.dequeue(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * If the message couldn't be sent, then reduce the number of available tries
	 * and pause the thread before making the next try
	 */
	private void onFailure() {
		// used another attempt
		triesRemaining--;
		try {
			// pause before trying again
			wait((long) DELAY * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
