package org.javarosa.core.services.storage;

public interface IStorageFactory {
	IStorageUtility newStorage (String name, Class type);
}
