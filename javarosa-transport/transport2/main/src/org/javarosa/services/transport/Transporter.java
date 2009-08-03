package org.javarosa.services.transport;

/**
 * 
 * A Transporter is given a TransportMessage in its constructor and has the
 * ability to send it via the send method
 * 
 * Example Transporter constructor: <code>
 * public SimpleHttpTransporter(SimpleHttpTransportMessage message) {
	this.message = message;
 * }
 * </code>
 * 
 * The TransportService spawns a thread which calls the send method. So
 * exceptions are caught and are recorded via message object methods:
 * <code>setFailureReason()</code> and <code>setStatus()</code>
 * 
 */
public interface Transporter {

	/**
	 * 
	 * (Attempt to) send the message given to the Transporter in its constructor
	 * 
	 * @return The message being sent
	 */
	public TransportMessage send();
	
	/**
	 * 
	 * (Attempt to) fetch data, given the info in the message given to the Transporter in its constructor
	 * 
	 * @return The message containing the info regarding the object being fetched
	 */
	public TransportMessage fetch();


	/**
	 * 
	 * 
	 * @return The message being sent
	 */
	public TransportMessage getMessage();

}
