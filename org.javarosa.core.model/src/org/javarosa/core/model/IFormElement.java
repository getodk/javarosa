package org.javarosa.core.model;

import java.util.Vector;

import org.javarosa.core.services.storage.utilities.Externalizable;

public interface IFormElement extends Externalizable {
	int getID ();
	void setID (int id);
	String getName ();
	void setName (String name);
	
	Vector getChildren ();
	void setChildren (Vector v);
	void addChild (IFormElement fe);
}
