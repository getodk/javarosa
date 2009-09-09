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
import org.javarosa.core.Context;

/**
 * The context for presenting pending referrals.
 * 
 * @author Clayton Sims
 * @date Feb 3, 2009 
 *
 */
public class PendingReferralsContext extends Context {
	private static final String PATIENT_ID = "PATIENT_ID";
	private static final String RET_EMPTY = "RETURN_ON_EMPTY";
	private static final String REF_FILTER = "REFERRAL_FILTER";
	
	
	public PendingReferralsContext(Context context) {
		super(context);
	}
	
	public int getPatientId() {
		Integer id = (Integer)this.getElement(PATIENT_ID);
		if(id == null) {
			return -1;
		} else {
			return id.intValue();
		}
	}
	
	public void setPatientId(int id) {
		this.setElement(PATIENT_ID, new Integer(id));
	}
	
	public boolean isReturnOnEmpty() {
		Boolean bool = (Boolean)this.getElement(RET_EMPTY);
		if(bool == null) {
			return false;
		} else {
			return bool.booleanValue();
		}
	}
	
	public void setReturnOnEmpty(boolean ret) {
		this.setElement(RET_EMPTY, new Boolean(ret));
	}
	
	public void setReferralFilter(IPatientReferralFilter ref) {
		this.setElement(REF_FILTER, ref);
	}
	
	public IPatientReferralFilter getReferralFilter() {
		IPatientReferralFilter filter = (IPatientReferralFilter)this.getElement(REF_FILTER);
		if(filter == null) {
			return new IPatientReferralFilter() {

				public boolean inFilter(PatientReferral ref) {
					// TODO Auto-generated method stub
					return ref.isPending();
				}
			};
		} else {
			return filter;
		}
	}
}
