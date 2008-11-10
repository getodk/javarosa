package org.javarosa.media.image.storage;

import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.services.storage.utilities.RMSUtility;

/**
 * A (potentially temporary) class for writing a basic byte[] to RMS
 * @author Cory Zue
 *
 */
public class FileRMSUtility extends RMSUtility {

	private Hashtable images;
	
	public FileRMSUtility(String name) {
		super(name, RMSUtility.RMS_TYPE_STANDARD);
		images = new Hashtable();	
	}
	
	public String[] getList() {
		String[] toReturn = new String[images.size()];
		int index = 0;
		Enumeration keys =images.keys(); 
		while (keys.hasMoreElements()) {
			toReturn[index]= (String) keys.nextElement();
			index++;
		}
		return toReturn;
	}
	
	public void saveImage(String fileName, byte[] data) {
		
		FileMetaData md = new FileMetaData();
		md.setFileName(fileName);
		this.writeBytesToRMS(data, md);
		images.put(fileName, md);
	}

}
