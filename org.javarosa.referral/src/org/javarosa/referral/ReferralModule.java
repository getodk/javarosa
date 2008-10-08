package org.javarosa.referral;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.referral.storage.ReferralRMSUtility;
import org.javarosa.referral.util.ReportingBindHandler;
import org.javarosa.xform.parse.XFormParser;

public class ReferralModule implements IModule {

	public void registerModule(Context context) {
		ReportingBindHandler reportingHandler = new ReportingBindHandler();
		XFormParser.registerBindHandler(reportingHandler);
		ReferralRMSUtility referralRms = new ReferralRMSUtility(ReferralRMSUtility.getUtilityName());
		
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(referralRms);

	}

}
