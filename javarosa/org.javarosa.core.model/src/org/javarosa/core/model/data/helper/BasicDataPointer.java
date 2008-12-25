package org.javarosa.core.model.data.helper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Basic implementor of the IDataPointer interface that keeps everything in memory
 * @author Cory Zue
 *
 */
public class BasicDataPointer implements IDataPointer {

	private byte[] data;
	private String name;
	public BasicDataPointer(String name, byte[] data) {
		this.name = name;
		this.data = data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#deleteData()
	 */
	public boolean deleteData() {
		
		this.data = null;
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getData()
	 */
	public byte[] getData() {
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getDisplayText()
	 */
	public String getDisplayText() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.data.IDataPointer#getDataStream()
	 */
	public InputStream getDataStream() {
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		return bis;
	} 


}
