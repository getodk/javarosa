package org.javarosa.services.transport;

import java.util.Date;

import de.enough.polish.io.Serializable;

public interface TransportMessage extends Serializable {

	/**
	 * 
	 * Each <code>TransportMessage</code> has the ability 
	 * to create a <code>Transporter</code> capable of sending itself
	 * 
	 * e.g. 
	 * <code>
	 * public Transporter getTransporter(){
	 * 	return new MyTransporter(this);
	 * }
	 * </code>
	 * 
	 * @return
	 */
	public Transporter getTransporter();

	/**
	 * ¨
	 * 
	 * @return Whatever is being sent
	 */
	public byte[] getContent();

	/**
	 * @return MimeType of that which is being sent
	 */
	public String getContentType();

	/**
	 * 
	 * 
	 * @return Whether sending has been successful or not
	 */
	public boolean isSuccess();
	
	/**
	 * 
	 * 
	 * @return
	 */
	public int getStatus();
	public void setStatus(int status);

	/**
	 * @return A message, often from an exception, describing the (most recent)
	 *         reason for failure
	 */
	public String getFailureReason();
	public void setFailureReason(String reason);

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
	
	public Date getCreated();
	
	
	

}
