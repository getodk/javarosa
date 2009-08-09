package org.javarosa.services.transport;


public interface TransportListener {

	void onChange(TransportMessage message,String remark);
	void onStatusChange(TransportMessage message);

}
