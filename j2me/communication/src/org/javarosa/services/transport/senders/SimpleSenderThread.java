package org.javarosa.services.transport.senders;

import org.javarosa.core.services.Logger;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.services.transport.TransportCache;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * 
 * A SimpleSenderThread takes a Transporter object and calls its send method
 * repeatedly until it succeeds, over a given number of tries, with a given
 * delay between each try
 * 
 */
public class SimpleSenderThread extends SenderThread {

	public SimpleSenderThread(Transporter transporter,
			TransportCache queue, int tries, int delay) {
		super(transporter, queue, tries, delay);

	}

	public SimpleSenderThread(Transporter transporter,
			TransportCache queue) {
		super(transporter, queue);

	}

	public void send() {
		TransportMessage message = this.transporter.getMessage();

		this.triesRemaining = this.tries;
		// try to send repeatedly for a given number of tries
		// or until the message has been successfully sent
		while ((this.triesRemaining > 0) && !message.isSuccess()) {
			try {
				message = attemptToSend();
			} catch (TransportException e) {
				Logger.exception("SimpleSenderThread.send", e);
				e.printStackTrace();
				// can't log and throw this from within the API
				// and since this is in a thread, nothing to do
			}
		}

		// if the loop was executed merely because the tries have been
		// used up, then the message becomes cached, for sending
		// via the "Send Unsent" user function
		if (!message.isSuccess()) {
			Logger.log("send", message.getTag() + " failed");
			message.setStatus(message.isCacheable() ? TransportMessageStatus.CACHED : TransportMessageStatus.FAILED);
			notifyStatusChange(message);
		} else {
			Logger.log("send", message.getTag() + " success");
		}
		
		
		if (message.isCacheable()) {
			try {
				this.messageStore.updateMessage(message);
			} catch (Exception e) {
				Logger.exception("SimpleSenderThread.send/isCacheable", e);
				e.printStackTrace();
				// if this update fails, the CACHED status
				// isn't permanent (the message doesn't fall through
				// a gap) because we note the duration of the SenderThread
				// and, should a message be found with the status CACHED
				// and yet was created before (now-sender thread duration)
				// then it is considered to have the CACHED status
			}
		}
	}

}
