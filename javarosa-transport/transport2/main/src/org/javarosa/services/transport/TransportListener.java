package org.javarosa.services.transport;

/**
 * TransportListeners are notified on success and failure
 * of transport attempts
 *
 */
public interface TransportListener {
	
	public static int FAILURE_DESTINATION = 0;
	public static int FAILURE_TRANSPORTING = 1;
	public static int FAILURE_UNKNOWN = 2;
	
	public static int SUCCESS = 4;
	
	public static int TRANSPORTING = 8;
	
	public static int CANCELED = 16;
	
	/**
	 * Handler called when a the provided message has been successfully transported
	 * 
	 * @param tmessage The message that has been transported
	 * @param response The response from the destination
	 */
	public void onSuccess(TransportMessage tmessage, byte[] response);
	
	/**
	 * Handler called when a message has failed to be transported
	 * 
	 * @param tmessage The message that has failed
	 * @param failureType A codified value describing the type of failure which has occurred 
	 * @param failureMessage A detailed message about the failure
	 */
	public void onFailure(TransportMessage tmessage, int failureType, String failureMessage);
	
	/**
	 * Handler called when a message has non-terminal feedback about its current transportation.
	 * 
	 * @param tmessage The message which is updating
	 * @param message A message about the status of transportation for tmessage
	 */
	public void onUpdate(TransportMessage tmessage, String message);
}
