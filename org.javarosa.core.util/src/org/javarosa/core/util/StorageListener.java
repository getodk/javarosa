package org.javarosa.core.util;

public interface StorageListener {
	/**
	 * Called when an error occurs during a data upload or download.
	 * 
	 * @param errorMessage - the error message.
	 * @param e - the exception, if any, that did lead to this error.
	 */
	public void errorOccured(String errorMessage, Exception e);
}
