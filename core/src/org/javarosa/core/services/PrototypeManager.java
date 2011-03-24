package org.javarosa.core.services;

import org.javarosa.core.util.PrefixTree;
import org.javarosa.core.util.externalizable.CannotCreateObjectException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class PrototypeManager {
	private static PrefixTree prototypes;
	private static PrototypeFactory staticDefault;
		
	public static void registerPrototype (String className) {
		getPrototypes().addString(className);
		
		try {
			PrototypeFactory.getInstance(Class.forName(className));
		} catch (ClassNotFoundException e) {
			throw new CannotCreateObjectException(className + ": not found");
		}
		rebuild();
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
	
	public static PrototypeFactory getDefault() {
		if(staticDefault == null) {
			rebuild();
		}
		return staticDefault;
	}
	
	private static void rebuild() {
		if(staticDefault == null) {
			staticDefault = new PrototypeFactory(getPrototypes());
			return;
		}
		synchronized(staticDefault) {
			staticDefault = new PrototypeFactory(getPrototypes());
		}
	}
	
}
