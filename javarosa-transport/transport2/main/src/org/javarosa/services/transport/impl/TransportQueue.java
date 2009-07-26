package org.javarosa.services.transport.impl;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.services.transport.TransportMessage;

import de.enough.polish.io.RmsStorage;

public class TransportQueue {
	private RmsStorage queue = new RmsStorage();
	private int transportQueueSize = -1;

	private static String RECENTLY_SENT_STORENAME = "JavaROSATransQSent";
	private static String Q_STORENAME = "JavaROSATransQ";
	private static String QID_STORENAME = "JavaROSATransQId";

	public int getTransportQueueSize() {
		if (this.transportQueueSize < 0) {
			this.transportQueueSize = getTransportQueue().size();
		}
		return this.transportQueueSize;
	}

	public Vector getTransportQueue() {
		Vector records = new Vector();
		try {
			records = (Vector) this.queue.read(Q_STORENAME);
		} catch (IOException e) {
			// storage doesn't yet exist (according to Polish)
		}
		return records;
	}
	
	public void enqueue(TransportMessage message) throws IOException {
		message.setQueueIdentifier(getNextQueueIdentifier());
		Vector records = readAll();
		records.addElement(message);
		saveAll(records);
	}
	
	public void dequeue(TransportMessage message) throws IOException {
		Vector records = readAll();
		TransportMessage m = find(message.getQueueIdentifier(),records);
		if(m==null)
			throw new IllegalArgumentException("No queued message with id="+message.getQueueIdentifier());
		records.removeElement(m);
		saveAll(records);
	}
	
	private TransportMessage find(String id, Vector records){
		for(int i=0;i<records.size();i++){
			TransportMessage message = (TransportMessage)records.elementAt(i);
			if(message.getQueueIdentifier().equals(id))
				return message;
		}
		return null;
	}
	
	private String getNextQueueIdentifier() throws IOException {
		Integer i = (Integer)this.queue.read(QID_STORENAME);
		if(i==null){
			i = new Integer(1);
			this.queue.save(i, QID_STORENAME);
			return i.toString();
		}else{
			Integer newI = new Integer(i.intValue()+1);
			this.queue.save(newI, QID_STORENAME);
			return newI.toString();
		}

	}
	
	public  Vector readAll() {
	 
		Vector records = new Vector();
		try {
			records = (Vector) queue.read(Q_STORENAME);
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
	private void saveAll(Vector records) throws IOException {
		try {
			this.queue.delete(Q_STORENAME);
		} catch (IOException e) {
			// storage didn't exist (according to Polish)
		}
		this.queue.save(records, Q_STORENAME);
		this.transportQueueSize = records.size();
	}

}
