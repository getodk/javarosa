package org.fcitmuk.communication.sms;

import java.io.DataInputStream;
import java.io.DataOutputStream;


/** 
 * Interface through which SMS server commnunicates with the application specific server.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface SMSServerListener {

	/**
	 * Called when a new message has been received.
	 * 
	 * @param dis - the stream to read from.
	 * @param dos - the stream to write to.
	 */
	public void processMessage(DataInputStream dis, DataOutputStream dos);
	
	/**
	 * Called when an error occurs during processing.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e);
}
