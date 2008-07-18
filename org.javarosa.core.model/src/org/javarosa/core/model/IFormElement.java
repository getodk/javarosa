package org.javarosa.core.model;

import java.util.Vector;

public interface IFormElement {
	int getID ();
	void setID (int id);
	String getName ();
	void setName (String name);
	
	Vector getChildren ();
	void setChildren (Vector v);
	void addChild (IFormElement fe);
}
