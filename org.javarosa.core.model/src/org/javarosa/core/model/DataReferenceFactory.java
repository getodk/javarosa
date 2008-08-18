package org.javarosa.core.model;

import org.javarosa.core.util.Map;

/**
 * Data Reference factory is a factory class for creating
 * new instances of IDataReference objects based on their 
 * class name. 
 * 
 * Data references are returned following the prototype 
 * design pattern, existing references clone themselves
 * instead of using a reflected constructor. 
 * 
 * @author Clayton Sims
 *
 */
public class DataReferenceFactory {
	Map prototypeMap = new Map();
	
	public void addDataReference(IDataReference reference) {
		prototypeMap.put(reference.getClass().getName(), reference);
	}
	
	public IDataReference getNewReference(String name) {
		IDataReference reference = (IDataReference)prototypeMap.get(name);
		if(reference != null) {
			return reference.clone();
		} else {
			return null;
		}
	}

}
