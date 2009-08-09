package org.javarosa.services.transport.impl;

/**
 * 
 * TransportMessages have three statuses:
 * 
 * QUEUED - attempts are currently being made by a thread to send the message
 * 
 * CACHED - initial attempts to send failed and the message is now persisted,
 * waiting for the user to initiate new sending attempts
 * 
 * SENT - message has been sent
 * 
 */
public class TransportMessageStatus {

	private TransportMessageStatus(){
		// private constructor
	}
	/**
	 * the message is in a QueuingThread
	 */
	public static final int QUEUED = 1;

	/**
	 * the message has failed in a QueuingThread and has not been sent
	 */
	public static final int CACHED = 2;

	/**
	 * the message has been sent
	 */
	public static final int SENT = 3;

}
