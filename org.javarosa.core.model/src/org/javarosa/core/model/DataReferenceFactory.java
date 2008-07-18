package org.javarosa.core.model;

import org.javarosa.core.util.Map;

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
