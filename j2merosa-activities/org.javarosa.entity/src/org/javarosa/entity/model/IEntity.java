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

package org.javarosa.entity.model;

import org.javarosa.core.services.storage.utilities.RMSUtility;

//representation of an entity for use in the entity select activity
//it reads in the object is represents through readEntity, and caches all the values it needs
//to provide the rest of the functionality in the interface

public interface IEntity {
	String entityType ();
	IEntity factory (int recordID);
	void readEntity (Object o);
	Object fetchRMS (RMSUtility rmsu);
	
	String getID();
	String getName();
	int getRecordID();
	
	boolean matchID (String key);
	boolean matchName (String key);

	String[] getShortFields ();
	String[] getLongFields (Object o);
	String[] getHeaders (boolean detailed);

}
