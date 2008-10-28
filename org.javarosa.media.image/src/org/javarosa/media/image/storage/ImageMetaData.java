package org.javarosa.media.image.storage;

import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
 * (Temporary?) class for storing image meta data
 * @author Cory Zue
 *
 */
public class ImageMetaData  extends MetaDataObject {

	
	private String fileName;

	public void setMetaDataParameters(Object originalObject) {
		// TODO
	}

	public String getFileName() {
		
		return fileName;
	}
	
	public void setFileName(String s) {
		fileName = s;
	}
}
