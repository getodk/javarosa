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

package org.javarosa.core.services.storage.utilities;

public interface IRecordStorage {
	
	public void openAsRecordStorage(String name, boolean createIfNotExist) throws RecordStorageException;
	
	public void closeRecordStore() throws RecordStorageException;
	
	public int getNextRecordID() throws RecordStorageException;

	public void addRecord(byte[] data, int i, int length) throws RecordStorageException;

	public void setRecord(int recordId, byte[] data, int i, int length) throws RecordStorageException;

	public void deleteRecord(int recordId) throws RecordStorageException;

	public void deleteRecordStore() throws RecordStorageException;

	public byte[] getRecord(int recordId) throws RecordStorageException;

	public int getNumRecords() throws RecordStorageException;

	public IRecordStoreEnumeration enumerateRecords() throws RecordStorageException;

	public float getSize() throws RecordStorageException;
	
	public float getSizeAvailable() throws RecordStorageException;
}
