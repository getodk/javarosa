package org.javarosa.services.transport;

public interface Transporter {
	
	/**
	 * 
	 * A Transporter is given a TransportMessage in its constructor
	 * and has the ability to send
	 * 
	 * @return The message being sent
	 */
	public TransportMessage send();
	
	/**
	 * 
	 * 
	 * @return The message being sent
	 */
	public TransportMessage getMessage();

}
