package org.javarosa.core.model;

import org.javarosa.core.util.Externalizable;

public interface IDataReference extends Externalizable { 
	Object getReference ();
	void setReference (Object o); 
	boolean referenceMatches(IDataReference reference);
	
	/** 
	 * @return a new object that is a copy of this data reference
	 */
	IDataReference clone();
}
