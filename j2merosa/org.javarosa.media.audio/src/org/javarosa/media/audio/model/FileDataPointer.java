package org.javarosa.media.audio.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.javarosa.core.data.IDataPointer;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
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
	 * NOTE: Only for serialization use.
	 */
	public FileDataPointer() {
		//You shouldn't be calling this unless you are deserializing.
	}
	
	/**s
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

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		fileName = in.readUTF();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.util.externalizable.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		out.writeUTF(fileName);
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.data.IDataPointer#getLength()
	 */
	public long getLength() {
		return FileUtility.getFileLength(fileName);
	}
}
