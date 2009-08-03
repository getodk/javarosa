package org.javarosa.services.transport;

/**
 * TransportListeners are notified on success and failure
 * of transport attempts
 *
 */
public interface TransportListener {
	
	public void onSuccess();
	public void onFailure();

}
