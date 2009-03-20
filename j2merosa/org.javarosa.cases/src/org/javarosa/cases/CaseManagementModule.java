/**
 * 
 */
package org.javarosa.cases;

import org.javarosa.cases.storage.CaseRmsUtility;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseManagementModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(new CaseRmsUtility(CaseRmsUtility.getUtilityName()));
	}

}
