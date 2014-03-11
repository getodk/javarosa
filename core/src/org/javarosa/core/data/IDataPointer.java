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

package org.javarosa.core.data;

import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.util.externalizable.Externalizable;

/**
 * A data pointer representing a pointer to a (usually) larger object in memory.
 *
 * @author Cory Zue
 *
 */
public interface IDataPointer extends Externalizable {

	/**
	 * Get a display string that represents this data.
	 * @return
	 */

	public String getDisplayText();

	/**
	 * Get the data from the underlying storage.  This should maybe be a stream instead of a byte[]
	 * @return
	 * @throws IOException
	 */
	public byte[] getData() throws IOException;

	/**
	 * Get the data from the underlying storage.
	 * @return
	 * @throws IOException
	 */
	public InputStream getDataStream() throws IOException;

	/**
	 * Deletes the underlying data from storage.
	 */
	public boolean deleteData();

	/**
	 * @return Gets the length of the data payload
	 */
	public long getLength();
}
