package org.javarosa.services.transport;


public interface Transporter {
	
	/**
	 * 
	 * A Transporter is given a TransportMessage in its constructor
	 * and has the ability to send it via the send method
	 * 
	 * Example Transporter constructor:
	 * <code>
	 * public SimpleHttpTransporter(SimpleHttpTransportMessage message) {
		this.message = message;
	 * }
	 * </code>
	 * 
	 * The TransportService spawns a thread which calls the send method.
	 * So exceptions are caught and are recorded via message object method:
	 * <code>setFailureReason()</code> and <code>setStatus()</code>
	 * 
	 * @return The message being sent
	 */
	public TransportMessage send();
	
	/**
	 * 
	 * 
	 * @return The message being sent
	 */
	public TransportMessage getMessage();

}
