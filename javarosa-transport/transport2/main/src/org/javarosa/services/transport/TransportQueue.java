package org.javarosa.services.transport;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import de.enough.polish.io.RmsStorage;

public class TransportQueue {

	/**
	 * These constants are used to identify objects in persistent storage
	 * 
	 * Q_STORENAME - the queue of messages to be sent RECENTLY_SENT_STORENAME -
	 * messages recently sent QID_STORENAME - storage for the message id counter
	 * 
	 */

	private static String Q_STORENAME = "JavaROSATransQ";
	private static String QID_STORENAME = "JavaROSATransQId";
	private static String RECENTLY_SENT_STORENAME = "JavaROSATransQSent";

	/**
	 * The persistent store - it is partitioned into three corresponding to the
	 * three constants above
	 */
	private RmsStorage queue = new RmsStorage();

	/**
	 * We cache the size (in terms of numbers of records) of each of the
	 * persistent store partitions
	 */
	private Hashtable cachedCounts = new Hashtable();

	// two vars to do with testing outside the emulator
	private Hashtable testingQueue = new Hashtable();
	private boolean testing = false;

	/**
	 * @param testing
	 */
	public TransportQueue(boolean testing) {
		this.testing = testing;
		
		// cache the counts first
		int queueSize = readAll(Q_STORENAME).size();
		this.cachedCounts.put(Q_STORENAME, new Integer(queueSize));
		int recentlySentSize = readAll(RECENTLY_SENT_STORENAME).size();
		this.cachedCounts.put(RECENTLY_SENT_STORENAME, new Integer(
				recentlySentSize));
	}

	/**
	 * @return
	 */
	public int getTransportQueueSize() {
		Integer size = (Integer) this.cachedCounts.get(Q_STORENAME);
		return size.intValue();
	}

	/**
	 * @return A Vector of TransportMessages waiting to be sent
	 */
	public Vector getTransportQueue() {
		return readAll(Q_STORENAME);
	}

	/**
	 * @return A Vector of TransportMessages recently sent
	 */
	public Vector getRecentlySentMessages() {
		return readAll(RECENTLY_SENT_STORENAME);
	}

	/**
	 * 
	 * Add a new message to the send queue
	 * 
	 * @param message
	 * @throws IOException
	 */
	public String enqueue(TransportMessage message) throws IOException {
		String id = getNextQueueIdentifier();
		message.setQueueIdentifier(id);
		Vector records = readAll(Q_STORENAME);
		records.addElement(message);
		saveAll(records, Q_STORENAME);
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
	public void dequeue(TransportMessage message) throws IOException {
		Vector records = readAll(Q_STORENAME);
		TransportMessage m = find(message.getQueueIdentifier(), records);
		if (m == null)
			throw new IllegalArgumentException("No queued message with id="
					+ message.getQueueIdentifier());
		records.removeElement(m);
		saveAll(records, Q_STORENAME);

		// if we're dequeuing a successfully sent message
		// then transfer it to the recently sent list
		if (message.isSuccess()) {
			Vector recentlySent = readAll(RECENTLY_SENT_STORENAME);
			if (recentlySent == null)
				recentlySent = new Vector();
			recentlySent.addElement(message);
			saveAll(recentlySent, RECENTLY_SENT_STORENAME);
		}

	}

	/**
	 * 
	 * 
	 * Given a vector of messages, find the message with the given id
	 * 
	 * 
	 * @param id
	 * @param records
	 * @return
	 */
	private TransportMessage find(String id, Vector records) {
		for (int i = 0; i < records.size(); i++) {
			TransportMessage message = (TransportMessage) records.elementAt(i);
			if (message.getQueueIdentifier().equals(id))
				return message;
		}
		return null;
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
		Vector records = readAll(Q_STORENAME);
		for (int i = 0; i < records.size(); i++) {
			TransportMessage message = (TransportMessage) records.elementAt(i);
			if (message.getQueueIdentifier().equals(id))
				return message;
		}

		Vector sentRecords = readAll(RECENTLY_SENT_STORENAME);
		for (int i = 0; i < sentRecords.size(); i++) {
			TransportMessage message = (TransportMessage) sentRecords
					.elementAt(i);
			if (message.getQueueIdentifier().equals(id))
				return message;
		}

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
	private String getNextQueueIdentifier() throws IOException {
		// get the most recently used id
		Vector v = readAll(QID_STORENAME);
		if ((v == null) || (v.size() == 0)) {
			// null means there wasn't one, so create, save and return one
			Integer i = new Integer(1);
			v = new Vector();
			v.addElement(i);
			saveAll(v, QID_STORENAME);
			return i.toString();
		} else {

			Integer i = (Integer) v.firstElement();

			// increment the count to create a new one, save it and return it
			Integer newI = new Integer(i.intValue() + 1);

			v.removeAllElements();
			v.addElement(newI);
			saveAll(v, QID_STORENAME);
			return newI.toString();
		}

	}

	/**
	 * @param store
	 * @return
	 */
	public Vector readAll(String store) {
		Vector records = new Vector();
		try {
			if (testing) {
				records = (Vector) testingQueue.get(store);
				if (records == null)
					return new Vector();
			} else
				records = (Vector) queue.read(store);
		} catch (IOException e) {
			// storage doesn't yet exist (according to Polish)
		}
		return records;
	}

	/**
	 * @param records
	 * @param c
	 * @throws IOException
	 */
	private void saveAll(Vector records, String store) throws IOException {
		if (testing) {
			this.testingQueue.put(store, records);

		} else {

			try {
				this.queue.delete(store);
			} catch (IOException e) {
				// storage didn't exist (according to Polish)
			}
			this.queue.save(records, store);
		}
		// update the cached count for this store
		this.cachedCounts.put(store, new Integer(records.size()));
	}

}
