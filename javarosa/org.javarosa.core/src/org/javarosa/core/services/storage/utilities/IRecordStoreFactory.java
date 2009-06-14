package org.javarosa.core.services.storage.utilities;

public interface IRecordStoreFactory {
	
	/** 
	 * @return a new interface for discrete record storage
	 */
	public IRecordStorage produceNewStore();
}
