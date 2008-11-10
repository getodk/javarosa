package org.javarosa.media.image.model;

import org.javarosa.core.model.data.IDataPointer;
import org.javarosa.media.image.utilities.FileUtility;

/**
 * Implementation of the data pointer that represents an underlying file on the file system.
 * 
 * @author Cory Zue
 *
 */
public class FileDataPointer implements IDataPointer {

	private String fileName;

	public FileDataPointer(String fileName) {
		this.fileName = fileName;
	}
	
	public byte[] getData() {
		return FileUtility.getFileData(fileName);
	}

	public String getDisplayText() {
		return fileName;
	}

	public void deleteData() {
		// TODO delete the file
	}

}
