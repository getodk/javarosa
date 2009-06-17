/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.referral;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.referral.properties.ReferralPropertyRules;
import org.javarosa.referral.storage.ReferralRMSUtility;
import org.javarosa.referral.util.ReportingBindHandler;
import org.javarosa.xform.parse.XFormParser;

public class ReferralModule implements IModule {

	public void registerModule(Context context) {
		ReportingBindHandler reportingHandler = new ReportingBindHandler();
		XFormParser.registerBindHandler(reportingHandler);
		ReferralRMSUtility referralRms = new ReferralRMSUtility(ReferralRMSUtility.getUtilityName());
		
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(referralRms);
		
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new ReferralPropertyRules());
		PropertyUtils.initializeProperty(ReferralPropertyRules.REFERRALS_ENABLED_PROPERTY, ReferralPropertyRules.REFERRALS_ENABLED);
	}

}
