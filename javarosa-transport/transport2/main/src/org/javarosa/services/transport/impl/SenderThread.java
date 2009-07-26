package org.javarosa.services.transport.impl;

import java.io.IOException;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;

public class SenderThread extends Thread {

	private Transporter transporter;
	private TransportQueue queue;

	private int tries = 5;
	private int delay = 60;

	public SenderThread(Transporter transporter, TransportQueue queue) {
		this.transporter = transporter;
		this.queue = queue;
	}

	public void run() {
		TransportMessage message = this.transporter.getMessage();
		while ((tries > 0) && !message.isSuccess()) {
			message = attemptToSend();
		}

	}

	private TransportMessage attemptToSend() {
		TransportMessage message = this.transporter.send();
		if (message.isSuccess()) {
			onSuccess(message);
		} else {
			onFailure();
		}
		return message;
	}

	private void onSuccess(TransportMessage message) {
		// remove from queue
		try {
			this.queue.dequeue(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void onFailure() {
		// used another attempt
		tries--;
		try {
			// pause before trying again
			wait((long) delay * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
