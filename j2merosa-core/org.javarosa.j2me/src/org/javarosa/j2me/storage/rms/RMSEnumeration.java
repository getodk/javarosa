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

package org.javarosa.j2me.storage.rms;

/* DEPRECATED */


import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;

public class RMSEnumeration implements IRecordStoreEnumeration {
	
	private final RecordEnumeration enumeration;
	
	public RMSEnumeration(RecordEnumeration en) {
		this.enumeration = en;
	}

	public void destroy() {
		enumeration.destroy();
	}

	public boolean hasNextElement() {
		return enumeration.hasNextElement();
	}

	public boolean hasPreviousElement() {
		return enumeration.hasPreviousElement();
	}

	public boolean isKeptUpdated() {
		return enumeration.isKeptUpdated();
	}

	public void keepUpdated(boolean arg0) {
		enumeration.keepUpdated(arg0);
	}

	public byte[] nextRecord() throws RecordStorageException {
			try {
				return enumeration.nextRecord();
			} catch (InvalidRecordIDException e) {
				throw new RecordStorageException(e.getMessage());
			} catch (RecordStoreNotOpenException e) {
				throw new RecordStorageException(e.getMessage());
			} catch (RecordStoreException e) {
				throw new RecordStorageException(e.getMessage());
			}
	}

	public int nextRecordId() throws RecordStorageException {
		try {
			return enumeration.nextRecordId();
		} catch (InvalidRecordIDException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	public int numRecords() {
		return enumeration.numRecords();
	}

	public byte[] previousRecord() throws RecordStorageException {
		try {
			return enumeration.previousRecord();
		} catch (InvalidRecordIDException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreNotOpenException e) {
			throw new RecordStorageException(e.getMessage());
		} catch (RecordStoreException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	public int previousRecordId() throws RecordStorageException {
		try {
			return enumeration.previousRecordId();
		} catch (InvalidRecordIDException e) {
			throw new RecordStorageException(e.getMessage());
		}
	}

	public void rebuild() {
		enumeration.rebuild();
	}

	public void reset() {
		enumeration.reset();
	}

}
