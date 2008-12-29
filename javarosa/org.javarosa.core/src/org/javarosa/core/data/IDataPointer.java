package org.javarosa.core.data;

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
	 */
	public byte[] getData();

	/**
	 * Get the data from the underlying storage.  
	 * @return
	 */
	public InputStream getDataStream();

	/**
	 * Deletes the underlying data from storage.
	 */
	public boolean deleteData();
}
