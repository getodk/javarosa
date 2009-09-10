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

import org.javarosa.core.services.storage.utilities.MetaDataObject;

/**
 * (Temporary?) class for storing image meta data
 * @author Cory Zue
 *
 */
public class FileMetaData  extends MetaDataObject {

	
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
