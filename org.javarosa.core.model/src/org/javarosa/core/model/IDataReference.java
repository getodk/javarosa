package org.javarosa.core.model;

import org.javarosa.core.util.Externalizable;

/**
 * An IDataReference is a reference to a value in a data
 * model.
 * 
 * @author Clayton Sims
 */
public interface IDataReference extends Externalizable {
	
	/**
	 * @return The data reference value
	 */
	Object getReference ();
	/**
	 * @param reference the data reference value to be set
	 */
	void setReference (Object reference); 
	
	/**
	 * @param reference A reference to be evaluated against this reference
	 * @return true if the given data reference is associated with the same
	 * data value as this reference.
	 */
	boolean referenceMatches(IDataReference reference);
	
	/** 
	 * @return a new object that is a copy of this data reference
	 */
	IDataReference clone();
}
