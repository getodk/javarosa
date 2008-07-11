package org.javarosa.core.services.storage;

/**
 * Storage Providers are responsible for managing
 * access to persistent storage sources.
 *  
 * @author Clayton Sims
 *
 */
public interface IStorageProvider {
	/**
	 * Gets the unique name for this storage provider
	 * @return A unique string that identifies this provider
	 */
	String getName();
}
