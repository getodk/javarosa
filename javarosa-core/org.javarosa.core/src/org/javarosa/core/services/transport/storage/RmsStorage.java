/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.services.transport.storage;

/* DEPRECATED */


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
	private String name;
	
	/**
	 * Name of the record store containing the messages
	 */
	private static final String RS_MSG = "RS_MSG";

	public RmsStorage () {
		this(RS_MSG);
	}
	
	public RmsStorage (String name) {
		this.name = name;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#saveMessage(org.openmrs.transport.TransportMessage)
	 */
	public synchronized void saveMessage(TransportMessage message) throws IOException {
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
	public synchronized void updateMessage(TransportMessage message) {
		int recordId = message.getRecordId();
		try {
			init();
			byte[] data = ExtUtil.serialize(message);
			this.messageRecordStore.setRecord(recordId, data, 0, data.length);
		} catch (Exception e) {
			//#if debug.output==verbose || debug.output==exception
			e.printStackTrace();
			System.out.println("exception message: " + e.getMessage());
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
	public synchronized void deleteMessage(int msgIndex) {
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
	private synchronized void init() throws RecordStorageException {
		if (this.messageRecordStore == null) {
			messageRecordStore = JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getRecordStoreFactory().produceNewStore();
			messageRecordStore.openAsRecordStorage(name, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openmrs.transport.Storage#close()
	 */
	public synchronized void close() {
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
	private synchronized TransportMessage loadMessage(int recordId) throws IOException, DeserializationException{
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

//	/**
//	 * Loads transport message with given record id from RMS
//	 * 
//	 * @param recordId
//	 * @throws IOException
//	 * @return
//	 */
//	public TransportMessage getMessage(int recordId) throws IOException, DeserializationException {
//		try {
//			init();
//			byte[] data = messageRecordStore.getRecord(recordId);
//			TransportMessage message = new TransportMessage();
//			ExtUtil.deserialize(data, message);
//			return message;
//		} catch (RecordStorageException e) {
//			System.out.println(e);
//			throw new IOException(e.getMessage());
//		}
//        catch (DeserializationException uee) {
//        	uee.printStackTrace();
//        	throw uee;
//        }
//	}
	
	/**
	 * @return
	 */
	public synchronized Vector getMessages() {
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
	public synchronized Enumeration messageElements() {
		return getMessages().elements();
	}

}
