package org.javarosa.services.transport.senders;

import java.util.Vector;

import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.impl.TransportMessageStore;

public class BulkSenderThread extends SenderThread {

	private Vector messages;

	public BulkSenderThread(Transporter transporter, Vector messages,
			TransportMessageStore queue, int tries, int delay) {
		super(transporter, queue, tries, delay);
		this.messages = messages;

	}

	public BulkSenderThread(Transporter transporter, Vector messages,
			TransportMessageStore queue) {
		super(transporter, queue);
		this.messages = messages;
	}

	public void send() {

		for (int i = 0; i < this.messages.size(); i++) {
			TransportMessage message = (TransportMessage) this.messages
					.elementAt(i);
			this.transporter.setMessage(message);

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

			if (message.isCacheable()) {
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
		}
	}

}
