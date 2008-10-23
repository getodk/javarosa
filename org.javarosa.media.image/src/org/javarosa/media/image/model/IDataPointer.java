package org.javarosa.media.image.model;


/**
 * A data pointer representing a pointer to a (usually) larger object in memory.  
 * 
 * @author Cory Zue
 *
 */
public interface IDataPointer {
	
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
	 * Deletes the underlying data from storage.
	 */
	public void deleteData();
}
