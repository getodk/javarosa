package org.javarosa.services.transport;

import java.util.Vector;

import org.javarosa.services.transport.impl.TransportException;

/**
 * The TransportService needs persistent storage.
 * 
 * The TransportCache interface define the persistent storage
 * in terms of a message cache
 *
 */
public interface TransportCache {

	/**
	 * 
	 * Put a message in storage by caching
	 * 
	 * @param message
	 * @return
	 * @throws TransportException
	 */
	String cache(TransportMessage message) throws TransportException;
	/**
	 * 
	 * Remove a message from permanent cache
	 * 
	 * @param message
	 * @throws TransportException
	 */
	void decache(TransportMessage message) throws TransportException;

	/**
	 * 
	 * The TransportCache will be able to update a message currently in store.
	 * 
	 * @param message
	 * @throws TransportException
	 */
	
	void updateMessage(TransportMessage message) throws TransportException;

	/**
	 * @param id
	 * @return
	 */
	TransportMessage findMessage(String id);

	int getCachedMessagesCount();

	/**
	 * 
	 * Obtain all messages currently in the cache
	 * 
	 * @return
	 */
	Vector getCachedMessages();

	/**
	 * Delete all messages currently in the cache
	 */
	void clearCache();

}
