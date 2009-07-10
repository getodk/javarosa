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
package org.javarosa.chsreferral.view;

import java.io.IOException;

import javax.microedition.lcdui.Form;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IView;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.patient.model.Patient;
import org.javarosa.patient.storage.PatientRMSUtility;

/**
 * A Screen to provide formatted details about an individual referral.
 * 
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
public class ReferralsDetailView extends Form implements IView {
	PatientReferral ref;

	public ReferralsDetailView(String title) {
		super(title);
	}
	
	/**
	 * @param ref the referral whose information should be presented to the user. 
	 */
	public void setReferral(PatientReferral ref) {
		this.ref = ref;
		
		PatientRMSUtility prms = (PatientRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(PatientRMSUtility.getUtilityName());
		Patient p = new Patient();
		try {
			prms.retrieveFromRMS(ref.getPatientId(), p);
		} catch (IOException e) {
		} catch (DeserializationException e) {
		}
				
		// Putting it in swahili.
		//#if commcare.lang.sw
		this.append("Jina: " + p.getName()); // Name
		this.append("Namba: " + p.getPatientIdentifier());
		this.append("Tarehe ya rufaa: " + DateUtils.formatDate(ref.getDateReferred(), DateUtils.FORMAT_HUMAN_READABLE_SHORT)); // Date of referral
		this.append("Aina ya rufaa: " + ref.getType()); // Referral type
		//#else
		this.append("Name: " + p.getName()); // Name
		this.append("ID: " + p.getPatientIdentifier());
		this.append("Date of referral: " + DateUtils.formatDate(ref.getDateReferred(), DateUtils.FORMAT_HUMAN_READABLE_SHORT)); // Date of referral
		this.append("Referral type: " + ref.getType()); // Referral type
		//#endif
	}
	
	public PatientReferral getReferral() {
		return ref;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IView#getScreenObject()
	 */
	public Object getScreenObject() {
		return this;
	}

}
