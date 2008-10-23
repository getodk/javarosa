package org.javarosa.storage.rms;

import org.javarosa.core.services.storage.utilities.IRecordStorage;
import org.javarosa.core.services.storage.utilities.IRecordStoreFactory;

public class RMSStorageFactory implements IRecordStoreFactory {

	public IRecordStorage produceNewStore() {
		return new RMSRecordStorage();
	}

}
