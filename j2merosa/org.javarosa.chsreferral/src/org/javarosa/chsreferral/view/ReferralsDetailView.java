/**
 * 
 */
package org.javarosa.chsreferral.view;

import javax.microedition.lcdui.Form;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.api.IView;

/**
 * @author Clayton Sims
 * @date Jan 23, 2009 
 *
 */
public class ReferralsDetailView extends Form implements IView {
	PatientReferral ref;

	public ReferralsDetailView(String title) {
		super(title);
	}
	
	public void setReferral(PatientReferral ref) {
		this.ref = ref;
		//TODO: Get patient info here somehow.
		// Putting it in swahili.
		//#if commcare.lang.sw
		this.append("Jina: " + ref.getPatientId()); // Name
		this.append("Tarehe ya rufaa: " + ref.getDateReferred()); // Date of referral
		this.append("Aina ya rufaa: " + ref.getType()); // Referral type
		//#else
		this.append("Name: " + ref.getPatientId()); // Name
		this.append("Date of referral: " + ref.getDateReferred()); // Date of referral
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
