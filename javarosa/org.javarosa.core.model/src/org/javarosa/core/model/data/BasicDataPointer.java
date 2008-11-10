package org.javarosa.core.model.data;

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
	public void deleteData() {
		
		this.data = null;
	}

	public byte[] getData() {
		return data;
	}

	public String getDisplayText() {
		return name;
	} 


}
