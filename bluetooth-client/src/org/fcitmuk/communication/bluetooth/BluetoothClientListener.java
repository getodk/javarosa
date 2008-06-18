package org.fcitmuk.communication.bluetooth;

/**
 * Interface through which the bluetooth client communicates to the user.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface BluetoothClientListener {
	/**
	 * Called when an error occurs during any bluetooth operation.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e);
}
