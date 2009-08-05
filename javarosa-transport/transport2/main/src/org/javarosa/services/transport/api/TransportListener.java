package org.javarosa.services.transport.api;

/**
 * TransportListeners are notified on success and failure of transport attempts
 * 
 */
public interface TransportListener {

	/**
	 * Handler called when a the provided message has been successfully
	 * transported
	 * 
	 * @param tmessage
	 *            The message that has been transported
	 * @param response
	 *            The response from the destination
	 */
	void onSuccess(TransportMessage tmessage, byte[] response);

	/**
	 * Handler called when a message has failed to be transported
	 * 
	 * @param tmessage
	 *            The message that has failed
	 * @param failureType
	 *            A codified value describing the type of failure which has
	 *            occurred
	 * @param failureMessage
	 *            A detailed message about the failure
	 */
	void onFailure(TransportMessage tmessage, int failureType,
			String failureMessage);

	/**
	 * Handler called when a message has non-terminal feedback about its current
	 * transportation.
	 * 
	 * @param tmessage
	 *            The message which is updating
	 * @param message
	 *            A message about the status of transportation for tmessage
	 */
	void onUpdate(TransportMessage tmessage, String message);
}
