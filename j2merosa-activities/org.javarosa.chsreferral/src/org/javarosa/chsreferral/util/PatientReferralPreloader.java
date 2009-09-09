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

/**
 * 
 */
package org.javarosa.chsreferral.util;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IPreloadHandler;

/**
 * Provides a preloader for form entry interactions to access information about 
 * existing patient referrals.
 * 
 * @author Clayton Sims
 * @date Feb 3, 2009 
 *
 */
public class PatientReferralPreloader implements IPreloadHandler {

	private final PatientReferral referral;
	
	/**
	 * Creates a preloader using the provided Referral.
	 * @param referral The object that should be used to preload data.
	 */
	public PatientReferralPreloader(PatientReferral referral) {
		this.referral = referral;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePostProcess(org.javarosa.core.model.instance.TreeElement, java.lang.String)
	 */
	public boolean handlePostProcess(TreeElement node, String params) {
		// No post-processing supported as of this time.
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePreload(java.lang.String)
	 */
	public IAnswerData handlePreload(String preloadParams) {
		if(preloadParams.equals("id")) {
			return new StringData(referral.getReferralId());
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "patient_referral";
	}

}
