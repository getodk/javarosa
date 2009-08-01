package org.javarosa.services.transport.listeners;

/**
 * 
 *The GetTransport initiates the activity to download information from the server
 *via its get method
 * 
 */
public interface IGetTransporter {
	
	/**
	 * 
	 * (Attempt to) get data from the server
	 * 
	 * @return The message being downloaded
	 */
	public IGetTransportMessage get();


}
