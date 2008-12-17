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
	
	public boolean deleteData() {
		
		this.data = null;
		return true;
	}

	public byte[] getData() {
		return data;
	}

	public String getDisplayText() {
		return name;
	} 
	public InputStream getDataStream() {
		// TODO Auto-generated method stub
		ByteArrayInputStream bis = new ByteArrayInputStream(data);
		return bis;
	} 


}
