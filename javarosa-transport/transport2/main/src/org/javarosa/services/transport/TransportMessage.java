package org.javarosa.services.transport;

import java.util.Date;

import org.javarosa.services.transport.impl.TransportMessageStatus;

import de.enough.polish.io.Serializable;

/**
 * TransportMessage is one of a pair of interfaces which must
 * be implemented in order to extend the Transport Layer
 * 
 * The other is Transporter
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
	 * public Transporter createTransporter(){
	 * 	    return <b>new</b> SimpleHttpTransporter(this);
	 * }
	 * </code>
	 * 
	 * @return A Transporter object able to send this message
	 */
	public Transporter createTransporter();

	/**
	 * ¨
	 * 
	 * @return Whatever is being sent
	 */
	public Object getContent();

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
	 * @return An integer representing the status of the message (whether queued, cached or sent)
	 * 
	 * @see TransportMessageStatus
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
	 * @return The queue-unique identifier assigned to the message
	 */
	public String getQueueIdentifier();

	/**
	 * @param id
	 */
	public void setQueueIdentifier(String id);
	
	/**
	 * 
	 * The TransportService first tries to send a message in a QueuingThread.
	 * 
	 * This poses an issue: if the QueuingThread is interrupted unexpectedly,
	 * a TransportMessage could be stuck with a QUEUED status and never get onto the 
	 * CACHED list to be sent via the "sendCached" method
	 * 
	 * To prevent Messages being stuck with a QUEUED status, a time-limit is set,
	 * after which they are considered to be CACHED
	 */
	public void setQueuingDeadline(long time);
	/**
	 * @return The time at which the TransportService concludes that the message is no longer
	 * within its initial QueuingThread
	 * 
	 * @see QueueingThread
	 */
	public long getQueuingDeadline();
	

	/**
	 * to be commented
	 */
	public Date getCreated();
	public Date getSent();
	
	public TransportListener getTransportListener();
	public void setTransportListener(TransportListener listener);

}
