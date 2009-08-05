package org.javarosa.services.transport.api;

import java.io.InputStream;
import java.util.Date;

import org.javarosa.services.transport.impl.TransportMessageStatus;

import de.enough.polish.io.Serializable;

/**
 * TransportMessage is one of a pair of interfaces which must be implemented in
 * order to extend the utility of the TransportService, the other being Transporter.
 * 
 * The other is Transporter
 * 
 */
public interface TransportMessage extends Serializable {

	/**
	 * 
	 * 
	 * Each <code>TransportMessage</code> has the ability to create a
	 * <code>Transporter</code> capable of sending itself
	 * 
	 * e.g. <code>
	 * public Transporter createTransporter(){
	 * 	    return <b>new</b> HttpTransporter(this);
	 * }
	 * </code>
	 * 
	 * @return A Transporter object able to send this message
	 */
	Transporter createTransporter();

	/**
	 * ¨
	 * 
	 * @return Whatever is being sent
	 */
	InputStream getContentStream();

	/**
	 * @return MimeType of that which is being sent
	 */
	String getContentType();

	/**
	 * 
	 * 
	 * @return An integer representing the status of the message
	 * 
	 * @see TransportMessageStatus
	 */
	int getStatus();

	/**
	 * to be commented
	 */
	Date getCreated();

	Date getSent();

	/**
	 * TransportListeners have methods which are called at various
	 * points in the transporting of a message.
	 * 
	 * @param listener
	 */
	void addTransportListener(TransportListener listener);

	/**
	 * Gets the response from the destination for this message.
	 * 
	 * NOTE: This value must be non-null for any object whose status has been
	 * changed from TransportListener.TRANSPORTING.
	 * 
	 * @return
	 */
	byte[] getResponse();

	void setCacheId(String cacheId);

	String getCacheId();
}
