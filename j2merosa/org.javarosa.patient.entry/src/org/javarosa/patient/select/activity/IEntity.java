package org.javarosa.patient.select.activity;

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
