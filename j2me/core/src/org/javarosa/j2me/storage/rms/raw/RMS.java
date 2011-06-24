package org.javarosa.j2me.storage.rms.raw;

import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExternalizableWrapper;

/**
 * A simple wrapper around an RMS RecordStore that handles common exceptions and
 * provides extra services like automatically opening/closing the RecordStore to
 * free up space.
 */
public class RMS {
	public RecordStore rms; // the RecordStore object being wrapped
	public String name; // the name of this RecordStore

	/**
	 * Open/create an RMS and wrap it
	 * 
	 * @param name
	 *            name of the RMS
	 * @param create
	 *            if true, create the RMS if it does not exist
	 * @throws RecordStoreException
	 *             any exception from openRecordStore() is passed on
	 *             transparently
	 */
	public RMS(String name, boolean create) throws RecordStoreException {
		this.name = name;
		this.rms = RecordStore.openRecordStore(name, create);
	}

	public int addRecord(byte[] data) {
		return addRecord(data, false);
	}

	/**
	 * Simple wrapper for RecordStore.addRecord().
	 * 
	 * Optionally, if, on first attempt, RecordStore is full, it will
	 * close/reopen the record store to free up any available space, then try
	 * once more. (This may have a hefty performance penalty)
	 * 
	 * @param data
	 *            record to add
	 * @param tryHard
	 *            if true, will close/reopen the record store on a 'full' error
	 *            and try again
	 * @return id of added record; -1 if full and no record was added
	 */
	public int addRecord(byte[] data, boolean tryHard) {
		try {
			int id = -1;

			try {
				id = rms.addRecord(data, 0, data.length);
			} catch (RecordStoreFullException rsfe) {
				// do nothing
			}

			if (id == -1 && tryHard) {
				cycle();
				try {
					id = rms.addRecord(data, 0, data.length);
				} catch (RecordStoreFullException rsfe2) {
					// do nothing
				}
			}

			return id;
		} catch (RecordStoreException rse) {
			throw new RuntimeException("Error adding record to RMS; "
					+ rse.getMessage());
		}
	}

	public boolean updateRecord(int id, byte[] data) {
		return updateRecord(id, data, false);
	}

	/**
	 * Simple wrapper for RecordStore.updateRecord().
	 * 
	 * Optionally, if, on first attempt, RecordStore is full, it will
	 * close/reopen the record store to free up any available space, then try
	 * once more. (This may have a hefty performance penalty)
	 * 
	 * BUG: on the Nokia 6085 (and probably others, the RMS becomes hosed if you
	 * try to update a record and run out of space, so 'tryHard' will not save
	 * you here
	 * 
	 * Error if no record for 'id' exists
	 * 
	 * @param id
	 *            id of record to update
	 * @param data
	 *            updated record data
	 * @param tryHard
	 *            if true, will close/reopen the record store on a 'full' error
	 *            and try again
	 * @return true if the record was updated; false if full
	 */
	public boolean updateRecord(int id, byte[] data, boolean tryHard) {
		try {
			boolean success = false;

			try {
				rms.setRecord(id, data, 0, data.length);
				success = true;
			} catch (RecordStoreFullException rsfe) {
				// do nothing
			}

			if (!success && tryHard) {
				cycle();
				try {
					rms.setRecord(id, data, 0, data.length);
					success = true;
				} catch (RecordStoreFullException rsfe2) {
					// do nothing
				}
			}

			return success;
		} catch (InvalidRecordIDException e) {
			throw new RuntimeException(
					"Attempted to update a record that does not exist [" + id
							+ "]");
		} catch (RecordStoreException e) {
			throw new RuntimeException("Error updating record in RMS; "
					+ e.getMessage());
		}
	}

	/**
	 * Return the byte data for a record.
	 * 
	 * @param id
	 *            record ID
	 * @return byte array of record's data; null if no record exists for that ID
	 */
	public byte[] readRecord(int id) {
		try {
			return rms.getRecord(id);
		} catch (InvalidRecordIDException iride) {
			return null;
		} catch (RecordStoreException rse) {
			throw new RuntimeException("Error reading record from RMS; "
					+ rse.getMessage());
		}
	}

	/**
	 * Return a deserialized record object
	 * 
	 * @param id
	 *            record ID
	 * @param type
	 *            object type of record
	 * @return record object; null if no record exists for that ID
	 */
	public Object readRecord(int id, Class type) {
		byte[] data = readRecord(id);
		try {
			return (data != null ? ExtUtil.deserialize(data, type) : null); // technically
																			// loses
																			// information
																			// for
																			// 'Nullable's
		} catch (DeserializationException de) {
			throw new RuntimeException(
					"Error deserializing bytestream for type ["
							+ type.getName() + "]; " + de.getMessage());
		}
	}

	/**
	 * Return a deserialized record object
	 * 
	 * @param id
	 *            record ID
	 * @param ew
	 *            ExternalizableWrapper for record type (should not use
	 *            ExtWrapNull(...), as you can't distinguish the record's data
	 *            being null from null as meaning record-not-found)
	 * @return record object; null if no record exists for that ID
	 */
	public Object readRecord(int id, ExternalizableWrapper ew) {
		byte[] data = readRecord(id);
		try {
			return (data != null ? ExtUtil.deserialize(data, ew) : null);
		} catch (DeserializationException de) {
			throw new RuntimeException("Error deserializing bytestream; "
					+ de.getMessage());
		}
	}

	/**
	 * Remove a record. Error if record does not exist
	 * 
	 * @param id
	 *            record ID to remove
	 */
	public void removeRecord(int id) {
		try {
			rms.deleteRecord(id);
		} catch (InvalidRecordIDException e) {
			throw new RuntimeException(
					"Attempted to remove a record that does not exist [" + id
							+ "]");
		} catch (RecordStoreException e) {
			throw new RuntimeException("Error removing record from RMS; "
					+ e.getMessage());
		}
	}

	/**
	 * Close this record store.
	 * 
	 * It will call closeRecordStore() as many times as necessary to ensure the
	 * record store is closed (this is necessary as, if the record store has
	 * been opened and never closed by other threads/StorageUtilities, our
	 * attempts to close it will have no effect; there must be one call to
	 * closeRecordStore() for each call to openRecordStore() for the record
	 * store to be truly closed).
	 */
	public void close() {
		boolean closed = false;

		while (!closed) {
			try {
				rms.closeRecordStore();
			} catch (RecordStoreNotOpenException e) {
				closed = true;
			} catch (RecordStoreException e) {
				throw new RuntimeException("Error closing recordstore");
			}
		}
	}

	/**
	 * Check that the record store is open, and reopen it if not. (It may have
	 * been closed by other threads or StorageUtilitys)
	 */
	public void ensureOpen() {
		try {
			rms.getName(); // presumably the simplest operation that will
							// trigger a 'not open' exception
		} catch (RecordStoreNotOpenException e) {
			try {
				rms = RecordStore.openRecordStore(name, false);
			} catch (RecordStoreException e1) {
				throw new RuntimeException("error");
			}
		}
	}

	/**
	 * Forcibly close and re-open the record store, to trigger reclamation of
	 * any unused space.
	 */
	public void cycle() {
		try {
			close();
			rms = RecordStore.openRecordStore(name, false);
		} catch (RecordStoreException e) {
			throw new RuntimeException("error cycling recordstore");
		}
	}
}