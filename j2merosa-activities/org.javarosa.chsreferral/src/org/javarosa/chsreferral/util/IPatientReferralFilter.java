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

/**
 * Provides an interface used to filter Patient Referrals for
 * a list of Referrals.
 * 
 * @author Clayton Sims
 * @date Feb 11, 2009 
 *
 */
public interface IPatientReferralFilter {
	
	/**
	 * Identifies whether the referral presented is a member of
	 * the filtered set.
	 *  
	 * @param ref The referral to be checked.
	 * @return True if the referral should be considered a member of
	 * the filtered set. False otherwise.
	 */
	public boolean inFilter(PatientReferral ref);
}
