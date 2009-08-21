/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
