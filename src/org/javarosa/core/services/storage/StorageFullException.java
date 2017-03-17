package org.javarosa.core.services.storage;

/**
 * An exception thrown by a StorageUtility when the requested action cannot be completed because there is not enough
 * space in the underlying device storage.
 */
public class StorageFullException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7803902267953954030L;

}
