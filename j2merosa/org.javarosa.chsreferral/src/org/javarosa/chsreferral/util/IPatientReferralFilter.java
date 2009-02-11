/**
 * 
 */
package org.javarosa.chsreferral.util;

import org.javarosa.chsreferral.model.PatientReferral;

/**
 * @author Clayton Sims
 * @date Feb 11, 2009 
 *
 */
public interface IPatientReferralFilter {
	public boolean inFilter(PatientReferral ref);
}
