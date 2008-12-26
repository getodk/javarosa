package org.javarosa.media.image.model;

import java.io.InputStream;

import org.javarosa.core.model.data.helper.IDataPointer;
import org.javarosa.media.image.utilities.FileUtility;

/**
 * Implementation of the data pointer that represents an underlying file on the file system.
 * 
 * @author Cory Zue
 *
 */
public class FileDataPointer implements IDataPointer {

	private String fileName;

	/**
	 * Create a FileDataPointer from a file name
	 * @param fileName
	 */
	public FileDataPointer(String fileName) {
		this.fileName = fileName;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getData()
	 */
	public byte[] getData() {
		return FileUtility.getFileData(fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getDataStream()
	 */
	public InputStream getDataStream() {
		return FileUtility.getFileDataStream(fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getDisplayText()
	 */
	public String getDisplayText() {
		return fileName;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#deleteData()
	 */
	public boolean deleteData() {
		return FileUtility.deleteFile(fileName);
	}

}
