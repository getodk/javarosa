package org.javarosa.services.transport.send.impl;

import org.javarosa.services.transport.download.HttpGetTransporter;
import org.javarosa.services.transport.listeners.IGetTransporter;
import org.javarosa.services.transport.listeners.ITransportManager;

public class TransportManager implements ITransportManager  {

	public IGetTransporter createGetTransporter(){
		return new HttpGetTransporter();
		
	}
}
