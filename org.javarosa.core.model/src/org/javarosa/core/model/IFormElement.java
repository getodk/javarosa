package org.javarosa.core.model;

import java.util.Vector;

import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.util.Externalizable;

public interface IFormElement extends Localizable, Externalizable {
	int getID ();
	void setID (int id);
	String getName ();
	void setName (String name);
	
	Vector getChildren ();
	void setChildren (Vector v);
	void addChild (IFormElement fe);
}
