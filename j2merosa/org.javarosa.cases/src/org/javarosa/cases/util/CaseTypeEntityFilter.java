/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.entity.model.IEntity;
import org.javarosa.entity.util.IEntityFilter;

/**
 * @author Clayton Sims
 * @date Mar 24, 2009 
 *
 */
public class CaseTypeEntityFilter implements IEntityFilter {
	
	String caseTypeId;
	
	public CaseTypeEntityFilter(String caseTypeId) {
		this.caseTypeId = caseTypeId;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(IEntity entity) {
		CaseEntity ce = (CaseEntity)entity;
		
		return ce.type.equals(caseTypeId);
	}

}
