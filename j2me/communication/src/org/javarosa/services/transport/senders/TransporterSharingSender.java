package org.javarosa.services.transport.senders;

import java.util.Vector;

import org.javarosa.core.services.Logger;
import org.javarosa.services.transport.TransportCache;
import org.javarosa.services.transport.TransportListener;
import org.javarosa.services.transport.TransportMessage;
import org.javarosa.services.transport.impl.TransportMessageStatus;

public class TransporterSharingSender {

	private Vector messages;
	private TransportCache cache;
	private TransportListener listener;
	boolean halted = false;

	public TransporterSharingSender() {
	}
	
	public void init(Vector messages, TransportCache store, TransportListener listener) {
		this.messages = messages;
		this.cache = store;
		this.listener = listener;
		halted = false;
	}

	public void send() {
		System.out.println("Ready to send: "+this.messages.size()+" messages");
		
		int numSuccessful = 0;
		for (int i = 0; i < this.messages.size(); i++) {
			if(halted) {return;}
			TransportMessage message = (TransportMessage) this.messages.elementAt(i);

			this.listener.onChange(message, "Preparing to send: " + message);
			message.send();

			if (message.isCacheable()) {
				// if the loop was executed merely because the tries have been
				// used up, then the message becomes cached, for sending
				// via the "Send Unsent" user function
				if (!message.isSuccess()) {
					Logger.log("send-all", "fail on " + (i + 1) + "/" + messages.size() + " " + message.getFailureReason());
					
					message.setStatus(TransportMessageStatus.CACHED);
					this.listener.onStatusChange(message);

					try {
						this.cache.updateMessage(message);
					} catch (Exception e) {
						Logger.exception("TransportSharingSender.send/failure", e);
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
					numSuccessful++;
					
					// SUCCESS - remove from cache
					this.listener.onStatusChange(message);
					try {
						this.cache.decache(message);
					} catch (Exception e) {
						Logger.exception("TransportSharingSender.send/success", e);
						e.printStackTrace();

					}
				}
			} else {
				Logger.log("sanity", "TransportSharingSender.send msg not cacheable");
			}
		}
		Logger.log("send-all", (numSuccessful == messages.size() ? "success" : numSuccessful + "/" + messages.size() + " successful"));
	}
	
	public void halt() {
		halted = true;
	}
	
}
