package org.javarosa.communication.bluetooth.server;

import java.io.*;

/** 
 * Interface through which bluetooth server commnunicates with the application specific server.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface BluetoothServerListener {
	
	/**
	 * Called when a new connection has been received.
	 * 
	 * @param dis - the stream to read from.
	 * @param dos - the stream to write to.
	 */
	public void processConnection(DataInputStream dis, DataOutputStream dos);
	
	/**
	 * Called when an error occurs during processing.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e);
}
