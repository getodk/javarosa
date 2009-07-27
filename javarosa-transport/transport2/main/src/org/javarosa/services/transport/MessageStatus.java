package org.javarosa.services.transport;

/**
 * 
 * 
 *
 */
public class MessageStatus {
	
	// when the message is in the SenderThread
	public final static int QUEUED=1;
	
	// when the message has failed in the SenderThread 
	// but is not yet sent
	public final static int CACHED=2;
	
	// when the message has been sent
	public final static int SENT=3;

}
