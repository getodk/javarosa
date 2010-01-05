/**
 * 
 */
package org.javarosa.core.services.storage;

/**
 * Accessor class to retain data type encapsulation for wrapped utilities while allowing
 * advanced access to underlying model.
 * 
 * @author ctsims
 *
 */
public class StorageUtilAccessor {
	public static IStorageUtility getStorage(WrappingStorageUtility utility) {
		return utility.storage;
	}
}
