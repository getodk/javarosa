package org.javarosa.services.transport.api;

import java.io.InputStream;
import java.util.Date;

import org.javarosa.services.transport.impl.TransportMessageStatus;
import org.javarosa.services.transport.listeners.TransportListener;

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
	 * 	    return <b>new</b> HttpTransporter(this);
	 * }
	 * </code>
	 * 
	 * @return A Transporter object able to send this message
	 */
	public ITransporter createTransporter();

	/**
	 * ¨
	 * 
	 * @return Whatever is being sent
	 */
	public InputStream getContentStream();

	/**
	 * @return MimeType of that which is being sent
	 */
	public String getContentType();
	
	/**
	 * 
	 * 
	 * @return An integer representing the status of the message
	 * 
	 * @see TransportMessageStatus
	 */
	public int getStatus();

	/**
	 * to be commented
	 */
	public Date getCreated();
	public Date getSent();

	public void addTransportListener(TransportListener listener);

	/**
	 * Gets the response from the destination for this message.
	 * 
	 *  NOTE: This value must be non-null for any object whose status
	 *  has been changed from TransportListener.TRANSPORTING. 
	 * 
	 * @return
	 */
	public byte[] getResponse();
}
