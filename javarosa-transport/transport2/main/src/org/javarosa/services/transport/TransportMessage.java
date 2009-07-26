package org.javarosa.services.transport;

import de.enough.polish.io.Serializable;

/**
 * this.transportQueueSiz provides the process which invokes the transport
 * layer with information regarding the status of the attempts to send.
 * 
 * Each different transport method will have a different implementation 
 * (e.g. the Http result will provide the Http response code in addition)
 *
 */
public interface TransportMessage extends Serializable {
	
	public final static int TRANSPORT_METHOD_HTTP=1;
	
	
	public int getTransportMethod();

	/**¨
	 * 
	 * @return What is being sent
	 */
	public byte[] getContent();
	
	
	/**
	 * @return MimeType of what is being sent
	 */
	public String getContentType();

	/**
	 * 
	 * 
	 * @return Whether sending has been successful or not
	 */
	public boolean isSuccess();

	/**
	 * @return A message, often from an exception, describing the (most recent) reason for failure
	 */
	public String getFailureReason();
	 
	/**
	 * @return The number of times unsuccessful attempts have been made to send 
	 */
	public int getFailureCount();
	 
	
	/**
	 * 
	 * Every message is persisted in the message queue before any attempt is made to send it
	 * When persisted in the message queue, it is given a unique id
	 * 
	 * @return
	 */
	public String getQueueIdentifier();
	public void setQueueIdentifier(String id);
	

}
