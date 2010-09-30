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

/**
 * 
 */
package org.javarosa.j2me.log;

import java.util.Date;
import java.util.Vector;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import org.javarosa.core.api.ILogger;
import org.javarosa.core.log.IAtomicLogSerializer;
import org.javarosa.core.log.IFullLogSerializer;
import org.javarosa.core.log.LogEntry;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.j2me.storage.rms.RMSStorageUtility;
import org.javarosa.j2me.util.SortedIntSet;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class J2MELogger implements ILogger {
	
	RMSStorageUtility logStorage;
	boolean storageBroken = false;
	
	public J2MELogger() {
		String storageName = LogEntry.STORAGE_KEY;
		for(int i = 0; i < 5 ; ++i) {
			try {
				logStorage = new RMSStorageUtility(storageName, LogEntry.class);
				if(!LogEntry.STORAGE_KEY.equals(storageName)) {
					this.log("logger", "Old log storage broken. New storage RMS: " + storageName, new Date());
				}
				return;
			} catch(IllegalStateException ise) {
				ise.printStackTrace();
				//The logger not working should never break anything. This error
				//signifies that the storage is broken in a pretty irreperable way, so
				//we'll just start a new storage.
				storageName += "F";
			} catch(Exception e) {
				e.printStackTrace();
				//Even worse, we don't even know what's going on.
				storageName += "E";
			}
		}
		//If we made it here, the storage is seriously messed up and we'll just skip
		//logging entirely
		storageBroken=true;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#clearLogs()
	 */
	public void clearLogs() {
		if(storageBroken) { return; };
		synchronized(logStorage) {
			if(!checkStorage()) { return; }
			//'destroy' not supported yet
			//logStorage.destroy();
			//logStorage = new RMSStorageUtility("LOG", IncidentLog.class);
			
			Vector ids = new Vector();
			IStorageIterator li = logStorage.iterate();
			while (li.hasMore()) {
				ids.addElement(new Integer(li.nextID()));
			}
			for (int i = 0; i < ids.size(); i++) {
				int id = ((Integer)ids.elementAt(i)).intValue();
				logStorage.remove(id);
			}
			
			log("logs", "cleared", new Date());
		}
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#logIncident(java.lang.String, java.lang.String, java.util.Date)
	 */
	public void log(String type, String message, Date logDate) {
		if(storageBroken) { return; };
		synchronized(logStorage) {
			LogEntry log = new LogEntry(type, message, logDate);
			try {
				logStorage.add(log);
			} catch (StorageFullException e) {
				throw new RuntimeException("uh-oh, storage full [incidentlog]"); //TODO: handle this
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#serializeLogs()
	 */
	public <T> T serializeLogs(IFullLogSerializer<T> serializer) {
		if(storageBroken) { return null; };
		synchronized(logStorage) {
			if(!checkStorage()) { return null; }
			
			SortedIntSet IDs = new SortedIntSet();
			IStorageIterator li = logStorage.iterate();
			while (li.hasMore()) {
				IDs.add(li.nextID());
			}
			
			Vector logs = new Vector();
			Vector<Integer> vIDs = IDs.getVector();
			for (int i = vIDs.size() - 1; i >= 0; i--) {
				logs.addElement(logStorage.read(vIDs.elementAt(i).intValue()));
			}
		
			LogEntry[] collection = new LogEntry[logs.size()];
			logs.copyInto(collection);
			return serializer.serializeLogs(collection);
		}
	}

	/**
	 * called when an attempt to write to the log fails
	 */
	public void panic () {
		final String LOG_PANIC = "LOG_PANIC";
		
		try {
			RecordStore store = RecordStore.openRecordStore(LOG_PANIC, true);
			
			int days = (int)(System.currentTimeMillis() / DateUtils.DAY_IN_MS);
			byte[] record = new byte[] {(byte)((days / 256) % 256), (byte)(days % 256)};
			store.addRecord(record, 0, record.length);
			
			store.closeRecordStore();
		} catch (RecordStoreException rse) {
			throw new WrappedException(rse);
		}
	}

	public boolean serializeLogs(IAtomicLogSerializer serializer) {
		if(storageBroken) { return false; };
		synchronized(logStorage) {
			if(!checkStorage()) { return false; }
			IStorageIterator li = logStorage.iterate();
			while (li.hasMore()) {
				if(!serializer.serializeLog((LogEntry)li.nextRecord()));
				//Panic?
				return false;
			}
			return true;
		}
	}

	public int logSize() {
		if(storageBroken) { return -1; };
		synchronized(logStorage) {
			if(!checkStorage()) { return -1; }
			return logStorage.getNumRecords();
		}
	}
	
	
	/**
	 * Check storage attempts to determine whether the storage for the logger
	 * is in a safe state and can be utilized without errors occurring. If
	 * the Log storage is not in a safe state, the logger shouldn't attempt to
	 * perform actions on it that might crash the app.
	 * 
	 * @return True if the log store is safe to manipulate. False otherwise. 
	 */
	private boolean checkStorage() {
		try{logStorage.checkNotCorrupt(); return true;}
		catch(Exception e) {
			System.out.println("Log Storage Corrupt. Attempting to repair");
			//storage isn't in good shape. Try to repair it.
			try{
				logStorage.repair();
				logStorage.checkNotCorrupt();
				this.log("logger", "Corrupted Log Storage Repaired.", new Date());
				return true;
			} catch(Exception ex) {
				System.out.println("Log Storage Corrupted and Cannot be Repaired");
				//Still isn't working. Bad scene, but nothing
				//to do about it.
				return false;
				//We should either throw a runtime exception here, or we should
				//keep trying. Possibly should just dump the old RecordStore
				//completely and just start over.
			}
		}
	}

	public void halt() {
		if(!storageBroken){ 
			try{
				logStorage.close();
			}catch(Exception e ) {
				System.out.println("Caught error while trying to close log storage");
			}
		}
	}
}