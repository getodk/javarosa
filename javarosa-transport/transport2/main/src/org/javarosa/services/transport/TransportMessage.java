package org.javarosa.services.transport;

import java.util.Date;

import de.enough.polish.io.Serializable;

/**
 * TransportMessage is one of a pair of interfaces which must
 * be implemented in order to extend the Transport Layer
 * 
 * The other is the Transporter interface
 *
 */
public interface TransportMessage extends Serializable {

	/**
	 * 
	 * 
	 * Each <code>TransportMessage</code> has the ability 
	 * to create a <code>Transporter</code> capable of sending itself
	 * 
	 * e.g. 
	 * <code>
	 * public Transporter getTransporter(){
	 * 	    return <b>new</b> MyTransporter(this);
	 * }
	 * </code>
	 * 
	 * @return
	 */
	public Transporter createTransporter();

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
	/**
	 * 
	 * The TransportService first tries to send a message in a QueuingThread.
	 * 
	 * This poses an issue: if the QueuingThread is interrupted unexpectedly,
	 * a Message could be stuck with a QUEUED status and never get onto the 
	 * CACHED list which is sent via the "sendCached" method
	 * 
	 * To prevent Messages being stuck with a QUEUED status, a time-limit is set,
	 * after which they are considered to be CACHED
	 * 
	 * @param date
	 */
	public void setQueuingDeadline(Date date);
	public Date getQueuingDeadline();
	
	
	

}
