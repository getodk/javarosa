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
