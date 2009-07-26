package org.javarosa.services.transport;

/**
 * TransportResult provides the process which invokes the transport
 * layer with information regarding the status of the attempts to send.
 * 
 * Each different transport method will have a different implementation 
 * (e.g. the Http result will provide the Http response code in addition)
 *
 */
public interface TransportResult {

	/**¨
	 * 
	 * @return What was being sent
	 */
	public byte[] getPayload();

	/**
	 * 
	 * @return Whether sending was successful or not
	 */
	public boolean isSuccess();

	/**
	 * @return A message, often from an exception, describing the (most recent) reason for failure
	 */
	public String getFailureReason();
	public void setFailureReason(String message);
	
	/**
	 * @return The number of times unsuccessful attempts have been made to send this payload to this destination
	 */
	public int getFailureCount();
	/**
	 * Increment the failure count by 1
	 */
	public void incrementFailureCount();
	

}
