/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * @author Clayton Sims
 * @date Mar 20, 2009 
 *
 */
public class AcceptAllFilter implements IEntityFilter {

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(IEntity entity) {
		// TODO Auto-generated method stub
		return true;
	}

}
