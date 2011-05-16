package org.javarosa.services.transport;


public interface TransportListener {

	/**
	 * @param message
	 * @param remark
	 */
	void onChange(TransportMessage message,String remark);
	/**
	 * @param message
	 */
	void onStatusChange(TransportMessage message);

}
