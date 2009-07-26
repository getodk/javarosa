package org.javarosa.services.transport;

import de.enough.polish.io.Serializable;

public interface TransportMessage extends Serializable {

	/**
	 * 
	 * Each TransportMessage has the ability to create a Transporter capable of sending it
	 * 
	 * @return
	 */
	public Transporter getTransporter();

	/**
	 * ¨
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
	 * @return A message, often from an exception, describing the (most recent)
	 *         reason for failure
	 */
	public String getFailureReason();

	/**
	 * @return The number of times unsuccessful attempts have been made to send
	 */
	public int getFailureCount();

	/**
	 * 
	 * Every message is persisted in the message queue before any attempt is
	 * made to send it When persisted in the message queue, it is given a unique
	 * id
	 * 
	 * @return
	 */
	public String getQueueIdentifier();

	public void setQueueIdentifier(String id);

}
