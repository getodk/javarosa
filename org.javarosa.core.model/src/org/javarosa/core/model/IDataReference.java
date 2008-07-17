package org.javarosa.core.model;

import org.javarosa.core.services.storage.utilities.Externalizable;

public interface IDataReference extends Externalizable { 
	Object getReference ();
	void setReference (Object o); 
	boolean referenceMatches(IDataReference reference);
}
