package org.javarosa.core.model.utils;

import org.javarosa.core.util.Map;

public class PrototypeFactory {
	private Map prototypes = new Map();
	
	public void addNewPrototype(String name, Class prototype) {
		prototypes.put(name, prototype);
	}
	
	Object getNewInstance(String prototypeName) throws IllegalAccessException, InstantiationException {
		return ((Class)prototypes.get(prototypeName)).newInstance();
	}
}
