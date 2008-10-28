package org.javarosa.j2me.storage.rms;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

public class RMSStorageModule implements IModule {

	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().setRecordStoreFactory(new RMSStorageFactory());
	}

}
