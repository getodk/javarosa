package org.javarosa.services.transport;

import java.io.InputStream;
import java.util.Date;

import org.javarosa.core.services.storage.Persistable;
import org.javarosa.services.transport.impl.TransportMessageStatus;

/**
 * TransportMessage is one of a pair of interfaces which must be implemented in
 * order to extend the Transport Layer
 * 
 * The other is Transporter
 * 
 */
public interface TransportMessage extends Persistable {

	public static final String STORAGE_NAME = "transport-message-store";
	
	/**
	 * 
	 * 
	 * Each <code>TransportMessage</code> has the ability to create a
	 * <code>Transporter</code> capable of sending itself
	 * 
	 * e.g. <code>
	 * public Transporter createTransporter(){
	 * 	    return <b>new</b> SimpleHttpTransporter(this);
	 * }
	 * </code>
	 * 
	 * @return A Transporter object able to send this message
	 */
	Transporter createTransporter();

	/**
	 * 
	 * Some transport types can sensibly make use of the TransportService's
	 * persistent store (e.g. http), some others not (e.g. sms) so that
	 * when transport fails, messages are cached to be retried later
	 * 
	 * This method returns true if this kind of Message is cacheable
	 * 
	 * @return
	 */
	boolean isCacheable();
	
	
	/**
	 *
	 * 
	 * @return Whatever is being sent
	 */
	Object getContent();
	
	InputStream getContentStream();

	/**
	 * 
	 * 
	 * @return Whether sending has been successful or not
	 */
	boolean isSuccess();

	/**
	 * 
	 * 
	 * @return An integer representing the status of the message (whether
	 *         queued, cached or sent)
	 * 
	 * @see TransportMessageStatus
	 */
	int getStatus();

	void setStatus(int status);

	/**
	 * @return A message, often from an exception, describing the (most recent)
	 *         reason for failure
	 */
	String getFailureReason();

	void setFailureReason(String reason);

	/**
	 * @return The number of times unsuccessful attempts have been made to send
	 */
	int getFailureCount();

	/**
	 * 
	 * Every message is persisted in the transport cache before any attempt is
	 * made to send it When persisted in the transport cache, it is given a unique
	 * id
	 * 
	 * @return The cache-unique identifier assigned to the message
	 */
	String getCacheIdentifier();

	/**
	 * @param id
	 */
	void setCacheIdentifier(String id);

	/**
	 * 
	 * The TransportService first tries to send a message in a SenderThread.
	 * 
	 * This poses an issue: if the SenderThread is interrupted unexpectedly, a
	 * TransportMessage could be stuck with a CACHED status and never get onto
	 * the CACHED list to be sent via the "sendCached" method
	 * 
	 * To prevent Messages being stuck with a QUEUED status, a time-limit is
	 * set, after which they are considered to be CACHED
	 */
	void setSendingThreadDeadline(long time);

	/**
	 * @return The time at which the TransportService concludes that the message
	 *         is no longer within its initial QueuingThread
	 * 
	 * @see QueueingThread
	 */
	long getQueuingDeadline();

	/**
	 * to be commented
	 */
	Date getCreated();

	Date getSent();

}
