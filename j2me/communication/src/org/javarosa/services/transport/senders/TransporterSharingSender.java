package org.javarosa.services.transport.senders;

import java.util.Vector;

import org.javarosa.services.transport.TransportCache;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.Transporter;
import org.javarosa.services.transport.impl.TransportMessageStatus;

public class TransporterSharingSender {

	private Vector messages;
	private Transporter transporter;
	private TransportCache cache;
	private TransportListener listener;
	boolean halted = false;

	public TransporterSharingSender() {
	}
	
	public void init(Transporter transporter, Vector messages,
			TransportCache store, TransportListener listener) {
		this.messages = messages;
		this.transporter = transporter;
		this.cache = store;
		this.listener = listener;
		halted = false;
	}

	public void send() {
		System.out.println("Ready to send: "+this.messages.size()+" messages by "+this.transporter);
		for (int i = 0; i < this.messages.size(); i++) {
			if(halted) {return;}
			TransportMessage message = (TransportMessage) this.messages
					.elementAt(i);
			this.transporter.setMessage(message);

			this.listener.onChange(message, "Preparing to send: " + message);
			message = this.transporter.send();

			if (message.isCacheable()) {
				// if the loop was executed merely because the tries have been
				// used up, then the message becomes cached, for sending
				// via the "Send Unsent" user function
				if (!message.isSuccess()) {
					message.setStatus(TransportMessageStatus.CACHED);
					this.listener.onStatusChange(message);

					try {
						this.cache.updateMessage(message);
					} catch (Exception e) {
						e.printStackTrace();
						// if this update fails, the SENDING status
						// isn't permanent (the message doesn't fall through
						// a gap) because we note the duration of the
						// SenderThread
						// and, should a message be found with the status SENDING
						// and yet was created before (now-queuing thread
						// duration)
						// then it is considered to have the SENDING status
					}
				} else {
					
					// SUCCESS - remove from cache
					this.listener.onStatusChange(message);
					try {
						this.cache.decache(message);
					} catch (Exception e) {
						e.printStackTrace();

					}
				}
			}
		}
	}
	
	public void halt() {
		halted = true;
	}

}
