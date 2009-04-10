/**
 * 
 */
package org.javarosa.j2me;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.j2me.log.J2MEIncidentLogger;
import org.javarosa.j2me.storage.rms.RMSStorageFactory;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class J2MEModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().setRecordStoreFactory(new RMSStorageFactory());
		JavaRosaServiceProvider.instance().registerIncidentLogger(new J2MEIncidentLogger());
	}

}
