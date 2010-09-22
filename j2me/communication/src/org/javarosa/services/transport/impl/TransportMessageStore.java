package org.javarosa.services.transport.impl;

import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.NoSuchElementException;
import java.util.Vector;

import org.javarosa.core.services.Logger;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.services.storage.StorageModifiedException;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.services.transport.TransportCache;
import org.javarosa.services.transport.TransportMessage;

/**
 * A TransportMessageStore is necessary since not all attempts to send succeed.
 * 
 * Every message given to the TransportService is persisted immediately, but
 * distinctions are made when querying the TransportMessageStore based on the
 * message status (i.e. the number of "cached" messages is not equal to the
 * number of messages in the store, but the number of messages in the store with
 * the status CACHED)
 * 
 */
public class TransportMessageStore implements TransportCache {

	/**
	 * These constants are used to identify objects in persistent storage
	 * 
	 * Q_STORENAME - the queue of messages to be sent RECENTLY_SENT_STORENAME -
	 * messages recently sent
	 * 
	 */

	public static final String Q_STORENAME = "JavaROSATransQ";
	public static final String RECENTLY_SENT_STORENAME = "JavaROSATransQSent";

	private static final int RECENTLY_SENT_STORE_MAXSIZE = 15;

	/**
	 * We cache the size (in terms of numbers of records) of each of the
	 * persistent store partitions
	 */
	private Hashtable cachedCounts;

	/**
	 * @param testing
	 */
	public TransportMessageStore() {
		cachedCounts = new Hashtable();
		storage(Q_STORENAME).repair();
		storage(RECENTLY_SENT_STORENAME).repair();
		updateCachedCounts();
	}

	/**
	 * @return
	 */
	public int getCachedMessagesCount() {
		Integer size = (Integer) this.cachedCounts.get(Integer
				.toString(TransportMessageStatus.CACHED));
		return size.intValue();
	}

	/**
	 * @return A Vector of TransportMessages waiting to be sent
	 */
	public Vector getCachedMessages() {
		Vector cached = new Vector();
		for(IStorageIterator en = storage(Q_STORENAME).iterate(); en.hasMore() ;) {
			TransportMessage message = (TransportMessage)en.nextRecord();
			if (message.getStatus() == TransportMessageStatus.CACHED) {
				cached.addElement(message);
			} else {
				if (isQueuingExpired(message)) {
					cached.addElement(message);
				}
			}
		}
		return cached;
	}

	/**
	 * 
	 * If a SenderThread is interrupted in some way, a message might not get the
	 * "Cached" status and be stuck with the "Queued" status instead
	 * 
	 * This method fixes that by checking the age of the message. If the age is
	 * greater than the time it would live in the SenderThread, then it should
	 * have the Cached status.
	 * 
	 * @param message
	 * @return
	 */
	private static boolean isQueuingExpired(TransportMessage message) {
		long now = new Date().getTime();
		long deadline = message.getQueuingDeadline();
		return (deadline > now);
	}

	/**
	 * 
	 * Add a new message to the send queue
	 * 
	 * @param message
	 * @throws IOException
	 */
	public String cache(TransportMessage message) throws TransportException {
		String id = getNextQueueIdentifier();
		message.setCacheIdentifier(id);
		message.setStatus(TransportMessageStatus.QUEUED);
		try {
			storage(Q_STORENAME).write(message);
		} catch (StorageFullException e) {
			throw new TransportException(e);
		}
		updateCachedCounts();
		return id;
	}

	/**
	 * 
	 * Remove a message from the send queue
	 * 
	 * 
	 * @param success
	 * @throws IOException
	 */
	public void decache(TransportMessage message) throws TransportException {
		
		try {
			storage(Q_STORENAME).remove(message);
		} catch(IllegalArgumentException iae) {
			//Already removed from the cache. Possibly by another sender. Not a problem.
			Logger._().log("transport", "Attempt to decache already decached message", new Date());
			return;
		} 
		message.setID(-1);

		// if we're dequeuing a successfully sent message
		// then transfer it to the recently sent list
		if (message.isSuccess()) {
			IStorageUtilityIndexed recent = storage(RECENTLY_SENT_STORENAME);
			// ensure that the recently sent store doesn't grow indefinitely
			// by limiting its size
			
			//With threads this gets tricky, since nextID can throw an exception if the iterator becomes invalid
			//before the next ID is retrieved. As such, we'll put this attempt to remove the message in
			//a loop and try to properly use the lock.
			
			boolean entered=false;
			int attempts = 0;
			
			while(attempts < 5 && !entered) {
				attempts++;
			
				if(recent.getNumRecords() == RECENTLY_SENT_STORE_MAXSIZE) {
					try {
						//I don't think we can simply grab the lock, since it's needed
						//by the actual storage processes
						int first = recent.iterate().nextID();
						recent.remove(first);
						
						//ITERATOR IS NOW INVALID
					} catch(StorageModifiedException sme) {
						//The iterator became invalid before it could properly remove the record
						//in question. Will try again.
						Logger._().log("transport", "Iterator invalidated before message removal", new Date());
					} catch(IllegalArgumentException iae) {
						//The record that was going to be removed was deleted before we
						//could get to it. Will try again.
					}
				}
				try {
					recent.write(message);
					entered = true;
				} catch (StorageFullException e) {
					throw new TransportException(e);
				}
			}
		}
		updateCachedCounts();
	}
	
	/**
	 * Given an id, look in the send queue and the recently sent queue,
	 * returning the message if it is found (null otherwise)
	 * 
	 * If the message is in the transport queue, then the success parameter will
	 * be false (and if found in the recentlySent queue, it will be set to true)
	 * 
	 * 
	 * @param id
	 * @return
	 */
	public TransportMessage findMessage(String id) {
		try{
			return (TransportMessage)storage(Q_STORENAME).getRecordForValue("cache-id", id);
		}
		catch(NoSuchElementException e) {
			//Not there. Not a big deal.
		}
		
		try{
			return (TransportMessage)storage(RECENTLY_SENT_STORENAME).getRecordForValue("cache-id", id);
		}
		catch(NoSuchElementException e) {
			//Not there. Not a big deal.
		}

		//Couldn't find it!
		return null;
	}

	/**
	 * 
	 * Get the next available queue identifier to assign to a newly queued
	 * message
	 * 
	 * 
	 * @return
	 * @throws IOException
	 */
	private String getNextQueueIdentifier() {
		String qid = PropertyUtils.genGUID(25);
		updateCachedCounts();
		return qid;
	}

	/**
	 * 
	 */
	private void updateCachedCounts() {
		int queued = 0;
		int cached = 0;
		// cache the counts first
		for(IStorageIterator en = storage(Q_STORENAME).iterate(); en.hasMore() ;) {
			TransportMessage message = (TransportMessage)en.nextRecord();
			if (message.getStatus() == TransportMessageStatus.QUEUED)
				queued++;
			if (message.getStatus() == TransportMessageStatus.CACHED)
				cached++;
			if (message.getStatus() == TransportMessageStatus.SENT)
				throw new RuntimeException("Sent message in the queue");
		}
		this.cachedCounts.put(Integer.toString(TransportMessageStatus.CACHED),
				new Integer(cached));
		this.cachedCounts.put(Integer.toString(TransportMessageStatus.QUEUED),
				new Integer(queued));

		// sent messages in another store
		int recentlySentSize = storage(RECENTLY_SENT_STORENAME).getNumRecords();
		this.cachedCounts.put(Integer.toString(TransportMessageStatus.QUEUED),
				new Integer(recentlySentSize));
	}

	/**
	 * @param message
	 * @throws IOException
	 */
	public void updateMessage(TransportMessage message) throws TransportException {
		try {
			if(message.getStatus() == TransportMessageStatus.CACHED) {
				IStorageUtilityIndexed cache = storage(Q_STORENAME);
				if(cache.getIDsForValue("cache-id",message.getCacheIdentifier()).size() > 0) {
					storage(Q_STORENAME).write(message);
				}
				updateCachedCounts();
			}
		} catch(StorageFullException e) {
			throw new TransportException(e);
		}
	}

	public void clearCache() {
		storage(Q_STORENAME).removeAll();
	}
	
	private IStorageUtilityIndexed storage(String name) {
		return (IStorageUtilityIndexed)StorageManager.getStorage(name);
	}
}
