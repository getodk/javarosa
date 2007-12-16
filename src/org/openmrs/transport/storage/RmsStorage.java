package org.openmrs.transport.storage;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.openmrs.transport.Serializer;
import org.openmrs.transport.Storage;
import org.openmrs.transport.TransportMessage;
import org.openmrs.transport.util.Logger;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class RmsStorage implements Storage, RecordListener {

	/**
	 * 
	 */
	private RecordStore messageRecordStore;

	/**
	 * Name of the record store containing the messages
	 */
	private static final String RS_MSG_NAME = "RS_MSG";

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#saveMessage(org.openmrs.transport.TransportMessage)
	 */
	public void saveMessage(TransportMessage message) throws IOException {
		Logger.log("Storing message in RecordStore");
		try {
			init();
			int recordId = this.messageRecordStore.getNextRecordID();
			message.setRecordId(recordId);
			byte[] data = Serializer.serialize(message);
			this.messageRecordStore.addRecord(data, 0, data.length);
			Logger.log("Message saved to RecordStore");
		} catch (RecordStoreException e) {
			Logger.log(e);
			throw new IOException(e.getMessage());
		} finally {
			close();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#updateMessage(org.openmrs.transport.TransportMessage)
	 */
	public void updateMessage(TransportMessage message) {
		int recordId = message.getRecordId();
		Logger.log("updateMessage(), id=" + recordId + ", status="
				+ message.getStatus());
		try {
			init();
			byte[] data = Serializer.serialize(message);
			Logger.log("Data length: " + data.length);
			this.messageRecordStore.setRecord(recordId, data, 0, data.length);
			Logger.log("Message updated");
		} catch (Exception e) {
			Logger.log(e);
		} finally {
			close();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#updateMessage(org.openmrs.transport.TransportMessage)
	 */
	public void deleteMessage(int msgIndex) {
		Logger.log("deleteMessage(), id=" + msgIndex);
		try {
			init();
			this.messageRecordStore.deleteRecord(msgIndex);
			Logger.log("Message deleted");
		} catch (Exception e) {
			Logger.log(e);
		} finally {
			close();
		}
	}

	/**
	 * @throws RecordStoreException
	 */
	private void init() throws RecordStoreException {
		if (this.messageRecordStore == null) {
			this.messageRecordStore = RecordStore.openRecordStore(RS_MSG_NAME,
					true);
			this.messageRecordStore.addRecordListener(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#close()
	 */
	public void close() {
		if (this.messageRecordStore != null) {
			try {
				this.messageRecordStore.removeRecordListener(this);
				this.messageRecordStore.closeRecordStore();
				this.messageRecordStore = null;
			} catch (RecordStoreException e) {
				Logger.log(e);
			}
		}
	}

	/**
	 * Called when a record has been updated
	 * 
	 * @param recordStore
	 * @param recordId
	 */
	public void recordAdded(RecordStore recordStore, int recordId) {
		Logger.log("Record added with id " + recordId);
	}

	/**
	 * Loads transport message with given record id from RMS
	 * 
	 * @param recordId
	 * @throws IOException
	 * @return
	 */
	private TransportMessage loadMessage(int recordId) throws IOException {
		try {
			init();
			byte[] data = messageRecordStore.getRecord(recordId);
			TransportMessage message = new TransportMessage();
			Serializer.deserialize(data, message);
			return message;
		} catch (RecordStoreException e) {
			Logger.log(e);
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Loads transport message with given record id from RMS
	 * 
	 * @param recordId
	 * @throws IOException
	 * @return
	 */
	public TransportMessage getMessage(int recordId) throws IOException {
		try {
			init();
			byte[] data = messageRecordStore.getRecord(recordId);
			TransportMessage message = new TransportMessage();
			Serializer.deserialize(data, message);
			return message;
		} catch (RecordStoreException e) {
			Logger.log(e);
			throw new IOException(e.getMessage());
		}
	}
	
	/**
	 * @return
	 */
	public Vector getMessages() {
		Vector messages = new Vector();
		RecordEnumeration en = null;
		try {
			init();
			en = messageRecordStore.enumerateRecords(null, null, true);
			while (en.hasNextElement()) {
				int recordId = en.nextRecordId();
				TransportMessage message = loadMessage(recordId);
				messages.addElement(message);
			}
		} catch (RecordStoreException e) {
			Logger.log(e);
			e.printStackTrace();
		} catch (IOException e) {
			Logger.log(e);
			e.printStackTrace();
		} finally {
			if (en != null) {
				en.destroy();
			}
			close();
		}
		return messages;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.rms.RecordListener#recordChanged(javax.microedition.rms.RecordStore,
	 *      int)
	 */
	public void recordChanged(RecordStore recordStore, int recordId) {
		Logger.log("record changed, id=" + recordId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.rms.RecordListener#recordDeleted(javax.microedition.rms.RecordStore,
	 *      int)
	 */
	public void recordDeleted(RecordStore recordStore, int recordId) {
		Logger.log("record deleted, id=" + recordId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#messageEnumeration()
	 */
	public Enumeration messageElements() {
		return getMessages().elements();
	}

}
