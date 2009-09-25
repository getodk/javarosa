/**
 * 
 */
package org.javarosa.chsreferral.util;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.entity.util.IEntityFilter;

/**
 * @author ctsims
 *
 */
public class ReferralEntityPendingFilter implements
		IEntityFilter<PatientReferral> {

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean matches(PatientReferral entity) {
		return entity.isPending();
	}

}
