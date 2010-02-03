package org.javarosa.core.services;

import org.javarosa.core.util.PrefixTree;
import org.javarosa.core.util.externalizable.CannotCreateObjectException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class PrototypeManager {
	private static PrefixTree prototypes;
		
	public static void registerPrototype (String className) {
		getPrototypes().addString(className);
		
		try {
			PrototypeFactory.getInstance(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new CannotCreateObjectException(className + ": not found");
		}
	}
	
	public static void registerPrototypes (String[] classNames) {
		for (int i = 0; i < classNames.length; i++)
			registerPrototype(classNames[i]);
	}
	
	public static PrefixTree getPrototypes () {
		if (prototypes == null) {
			prototypes = new PrefixTree();
		}
		return prototypes;
	}
	
}
