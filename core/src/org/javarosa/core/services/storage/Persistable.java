package org.javarosa.core.services.storage;

import org.javarosa.core.util.externalizable.Externalizable;

/**
 * A modest extension to Externalizable which identifies objects that have the concept of an internal 'record ID'
 */
public interface Persistable extends Externalizable {
	void setID (int ID);
	int getID ();
}
