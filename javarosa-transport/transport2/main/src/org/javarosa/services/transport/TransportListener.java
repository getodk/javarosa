package org.javarosa.services.transport;

public interface TransportListener {
	
	
	void onSuccess();
	void onFailure();
	void onUpdate();

}
