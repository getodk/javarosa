package org.javarosa.core.services.transport.storage;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.storage.utilities.IRecordStorage;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.services.transport.Storage;
import org.javarosa.core.services.transport.TransportMessage;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * A Storage type for Transport Messages that utilizes RMS storage
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class RmsStorage implements Storage {

	/**
	 * 
	 */
	private IRecordStorage messageRecordStore;

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
		} catch (RecordStorageException e) {
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
	private void init() throws RecordStorageException {
		if (this.messageRecordStore == null) {
			messageRecordStore = JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getRecordStoreFactory().produceNewStore();
			messageRecordStore.openAsRecordStorage(RS_MSG_NAME, true);
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
				this.messageRecordStore.closeRecordStore();
				this.messageRecordStore = null;
			} catch (RecordStorageException e) {
				//#if debug.output==verbose || debug.output==exception
				System.out.println(e);
				//#endif
			}
		}
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
			PrototypeFactory factory = new PrototypeFactory(JavaRosaServiceProvider.instance().getPrototypes());
			message = (TransportMessage)ExtUtil.deserialize(data, message.getClass(), factory);
			return message;
		} catch (RecordStorageException e) {
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
		} catch (RecordStorageException e) {
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
		IRecordStoreEnumeration en = null;
		try {
			init();
			en = messageRecordStore.enumerateRecords();
			while (en.hasNextElement()) {
				int recordId = en.nextRecordId();
				TransportMessage message = loadMessage(recordId);
				messages.addElement(message);
			}
		} catch (RecordStorageException e) {
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
	 * @see org.openmrs.transport.Storage#messageEnumeration()
	 */
	public Enumeration messageElements() {
		return getMessages().elements();
	}

}
