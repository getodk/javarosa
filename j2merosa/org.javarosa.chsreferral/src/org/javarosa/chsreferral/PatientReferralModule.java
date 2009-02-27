package org.javarosa.chsreferral;

import org.javarosa.chsreferral.storage.PatientReferralRMSUtility;
import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

/**
 * 
 */

/**
 * The Patient referral module registers the rms storage objects and other 
 * elements necessary to keep track of pending patient referrals.
 * 
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
public class PatientReferralModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		PatientReferralRMSUtility refRms = new PatientReferralRMSUtility(PatientReferralRMSUtility.getUtilityName());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(refRms);
	}

}
