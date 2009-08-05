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

	/**
	 * the message has failed in a QueuingThread
	 * and has not been sent
	 */
	public final static int CACHED = 1;

	public static int COMPLETED = 2;
	
	public static int TRANSPORTING = 4;
	
	public static int CANCELED = 8;

	public static int FAILURE_DESTINATION = 16;
	
	public static int FAILURE_TRANSPORTING = 32;
	
	public static int FAILURE_UNKNOWN =64;

}
