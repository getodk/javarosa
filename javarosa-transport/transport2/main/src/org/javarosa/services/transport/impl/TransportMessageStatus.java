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
	 * the message is in a QueuingThread
	 */
	public final static int QUEUED = 1;

	/**
	 * the message has failed in a QueuingThread
	 * and has not been sent
	 */
	public final static int CACHED = 2;

	/**
	 * the message has been sent
	 */
	public final static int SENT = 3;

	public static final int DOWNLOADED = 4;

	public static final int FAILED = 5;
	
	
	public static int SUCCESS = 6;
	
	public static int TRANSPORTING = 7;
	
	public static int CANCELED = 8;

	public static int FAILURE_DESTINATION = 9;
	
	public static int FAILURE_TRANSPORTING = 10;
	
	public static int FAILURE_UNKNOWN =11;

}
