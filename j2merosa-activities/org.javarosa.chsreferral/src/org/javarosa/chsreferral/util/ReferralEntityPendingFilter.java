/**
 * 
 */
package org.javarosa.chsreferral.util;

import org.javarosa.entity.util.IEntityFilter;

/**
 * @author ctsims
 *
 */
public class ReferralEntityPendingFilter implements
		IEntityFilter<ReferralEntity> {

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(ReferralEntity entity) {
		return entity.isPending();
	}

}
