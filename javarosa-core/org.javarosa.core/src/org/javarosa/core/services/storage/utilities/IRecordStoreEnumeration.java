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


public interface IRecordStoreEnumeration {

	public void destroy();

	public boolean hasNextElement();
	
	public boolean hasPreviousElement();

	public boolean isKeptUpdated();

	public void keepUpdated(boolean arg0);

	public byte[] nextRecord() throws RecordStorageException;

	public int nextRecordId() throws RecordStorageException;

	public int numRecords();

	public byte[] previousRecord() throws RecordStorageException;

	public int previousRecordId() throws RecordStorageException;

	public void rebuild();

	public void reset();

}
