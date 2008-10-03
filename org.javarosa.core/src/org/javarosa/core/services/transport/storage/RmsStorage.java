package org.javarosa.core.services.transport.storage;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordListener;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.javarosa.core.services.transport.Storage;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;

/**
 * A Storage type for Transport Messages that utilizes RMS storage
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
		try {
			init();
			int recordId = this.messageRecordStore.getNextRecordID();
			message.setRecordId(recordId);
			byte[] data = ExtUtil.serialize(message);
			this.messageRecordStore.addRecord(data, 0, data.length);
		} catch (RecordStoreException e) {
			//#if debug.output==verbose || debug.output==exception
			System.out.println(e);
			//#endif
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
		try {
			init();
			byte[] data = ExtUtil.serialize(message);
			this.messageRecordStore.setRecord(recordId, data, 0, data.length);
		} catch (Exception e) {
			//#if debug.output==verbose || debug.output==exception
			System.out.println(e);
			//#endif
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
		try {
			init();
			this.messageRecordStore.deleteRecord(msgIndex);
		} catch (Exception e) {
			//#if debug.output==verbose || debug.output==exception
			System.out.println(e);
			//#endif
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
				//#if debug.output==verbose || debug.output==exception
				System.out.println(e);
				//#endif
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
		//#if debug.output==verbose
		System.out.println("Record added with id " + recordId);
		//#endif
	}

	/**
	 * Loads transport message with given record id from RMS
	 * 
	 * @param recordId
	 * @throws IOException
	 * @return
	 */
	private TransportMessage loadMessage(int recordId) throws IOException, DeserializationException{
		try {
			init();
			byte[] data = messageRecordStore.getRecord(recordId);
			TransportMessage message = new TransportMessage();
			ExtUtil.deserialize(data, message);
			return message;
		} catch (RecordStoreException e) {
			//#if debug.output==verbose || debug.output==exception
			System.out.println(e);
			//#endif
			throw new IOException(e.getMessage());
		}
        catch (DeserializationException uee) {
        	uee.printStackTrace();
        	throw uee;
        }

	}

	/**
	 * Loads transport message with given record id from RMS
	 * 
	 * @param recordId
	 * @throws IOException
	 * @return
	 */
	public TransportMessage getMessage(int recordId) throws IOException, DeserializationException {
		try {
			init();
			byte[] data = messageRecordStore.getRecord(recordId);
			TransportMessage message = new TransportMessage();
			ExtUtil.deserialize(data, message);
			return message;
		} catch (RecordStoreException e) {
			System.out.println(e);
			throw new IOException(e.getMessage());
		}
        catch (DeserializationException uee) {
        	uee.printStackTrace();
        	throw uee;
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
			System.out.println(e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e);
			e.printStackTrace();
		} 
        catch (DeserializationException uee) {
        	uee.printStackTrace();
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
		//#if debug.output==verbose
		System.out.println("record changed, id=" + recordId);
		//#endif
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.microedition.rms.RecordListener#recordDeleted(javax.microedition.rms.RecordStore,
	 *      int)
	 */
	public void recordDeleted(RecordStore recordStore, int recordId) {
		//#if debug.output==verbose
		System.out.println("record deleted, id=" + recordId);
		//#endif
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
