/**
 * 
 */
package org.javarosa.user.activity;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.user.storage.UserRMSUtility;

/**
 * @author Clayton Sims
 * @date Mar 5, 2009 
 *
 */
public class UserModule implements IModule {

	public void registerModule(Context context) {
		UserRMSUtility UserRms = new UserRMSUtility(UserRMSUtility.getUtilityName());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(UserRms);
	}

}
