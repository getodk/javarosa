package org.javarosa.communication;

import org.javarosa.util.db.*;

/**
 * Interface through which the transport layer communicates to the user.
 * 
 * @author Daniel Kayiwa
 *
 */
public interface TransportLayerListener {
	
	/**
	 * Called after data has been successfully uploaded.
	 * 
	 * @param dataOutParams - parameters sent after data has been uploaded.
	 * @param dataOut - data sent after the upload.
	 */
	public void uploaded(Persistent dataOutParams, Persistent dataOut);
	
	/**
	 * Called after data has been successfully downloaded.
	 * 
	 * @param dataOutParams - the parameters sent with the data.
	 * @param dataOut - the downloaded data.
	 */
	public void downloaded(Persistent dataOutParams, Persistent dataOut);
	
	/**
	 * Called when an error occurs during a data upload or download.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e);
	
	public void cancelled();
}
