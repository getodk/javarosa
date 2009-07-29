package org.javarosa.services.transport;

/**
 * 
 * 
 *
 */
public class TransportMessageStatus {
	
	// when the message is in a QueuingThread
	public final static int QUEUED=1;
	
	// when the message has failed in a QueuingThread 
	// but is not yet sent
	public final static int CACHED=2;
	
	// when the message has been sent
	public final static int SENT=3;

}
