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

import org.javarosa.core.api.IIncidentLogger;
import org.javarosa.core.log.ILogSerializer;
import org.javarosa.core.log.IncidentLog;
import org.javarosa.core.log.WrappedException;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.IStorageIterator;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.j2me.storage.rms.RMSStorageUtility;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class J2MEIncidentLogger implements IIncidentLogger {
		
	IStorageUtility logStorage;
	
	public J2MEIncidentLogger() {
		logStorage = new RMSStorageUtility("LOG", IncidentLog.class);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#clearLogs()
	 */
	public void clearLogs() {
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
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#logIncident(java.lang.String, java.lang.String, java.util.Date)
	 */
	public void logIncident(String type, String message, Date logDate) {
		IncidentLog log = new IncidentLog(type, message, logDate);
		try {
			logStorage.add(log);
		} catch (StorageFullException e) {
			throw new RuntimeException("uh-oh, storage full [incidentlog]"); //TODO: handle this
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IIncidentLogger#serializeLogs()
	 */
	public <T> T serializeLogs(ILogSerializer<T> serializer) {
		Vector logs = new Vector();
		IStorageIterator li = logStorage.iterate();
		while (li.hasMore()) {
			logs.addElement(li.nextRecord());
		}
		IncidentLog[] collection = new IncidentLog[logs.size()];
		logs.copyInto(collection);
		return serializer.serializeLogs(collection);
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
	
}
