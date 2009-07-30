package org.javarosa.services.transport.threading;

import org.javarosa.services.transport.impl.TransportException;
import org.javarosa.services.transport.listeners.IGetTransportMessage;
import org.javarosa.services.transport.listeners.IGetTransporter;
import org.javarosa.services.transport.listeners.IOnDataReturned;
import org.javarosa.services.transport.listeners.IOnDataReturnedListener;

public class GetQueuingThread extends Thread implements IOnDataReturned {
	
	private IGetTransporter getTransporter;
	
	private IOnDataReturnedListener listener;

	public final static int DEFAULT_TRIES = 5;

	public final static int DEFAULT_DELAY = 60;
	
	private int tries = DEFAULT_TRIES;
	private int delay = DEFAULT_DELAY;

	private int triesRemaining;
	
	public GetQueuingThread(IOnDataReturnedListener listener, IGetTransporter transporter, int tries, int delay){
		this.getTransporter = transporter;
		this.tries = tries;
		this.delay = delay;
		
		this.listener = listener;
	}
	
	public void run() {
		IGetTransportMessage message = this.getTransporter.get();

		this.triesRemaining = this.tries;
		// try to send repeatedly for a given number of tries
		// or until the message has been successfully received
		while ((this.triesRemaining > 0) && !message.isSuccess()) {
			try {
				message = attemptToGet();
			} catch (TransportException e) {
				e.printStackTrace();
				// can't log and throw this from within the API
				// and since this is in a thread, nothing to do
			}
		}

	}

	private IGetTransportMessage attemptToGet() throws TransportException {
		
		IGetTransportMessage message = this.getTransporter.get();
		
		if(message.isSuccess()){
			onDataReturned(message);
		}else{
			onFailure();
		}
		return message;
	}
	
	public void onDataReturned(IGetTransportMessage message) {
		this.listener.onDataReceived(message);
		
	}
	
	/**
	 * If the message couldn't be downloaded, then reduce the number of available
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

}
