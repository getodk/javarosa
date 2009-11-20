package org.javarosa.core.services.storage;

import java.util.Hashtable;

public interface IMetaData {

	String[] getMetaDataFields ();
	Hashtable getMetaData(); //<String, E>
	Object getMetaData(String fieldName);
	
}
