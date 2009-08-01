package org.javarosa.services.transport.service;

import org.javarosa.services.transport.listeners.IGetTransportMessage;
import org.javarosa.services.transport.listeners.IGetTransporter;
import org.javarosa.services.transport.listeners.IOnDataReturnedListener;
import org.javarosa.services.transport.listeners.ITransportManager;
import org.javarosa.services.transport.threading.GetQueuingThread;

public class GetTransportService implements IOnDataReturnedListener {
	
	private IOnDataReturnedListener listener;

	public GetTransportService(IOnDataReturnedListener listener){
		this.listener = listener;
	}
	
	public GetQueuingThread get(ITransportManager transportManager){
		return get(transportManager, GetQueuingThread.DEFAULT_TRIES, GetQueuingThread.DEFAULT_DELAY);
	}
	
	public GetQueuingThread get(ITransportManager transportManager, int tries, int delay){
		IGetTransporter transporter = transportManager.createGetTransporter();
		
		GetQueuingThread thread = new GetQueuingThread(this, transporter, tries, delay);
		
		thread.start();
		
		return thread;
		
	}

	public void onDataReceived(IGetTransportMessage message) {
		listener.onDataReceived(message);
		
	}	
	
}
