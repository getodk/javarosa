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

package org.javarosa.chsreferral;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.api.IModule;
import org.javarosa.core.services.storage.StorageManager;

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
	public void registerModule() {
		StorageManager.registerStorage(PatientReferral.STORAGE_KEY, PatientReferral.class);
	}

}
