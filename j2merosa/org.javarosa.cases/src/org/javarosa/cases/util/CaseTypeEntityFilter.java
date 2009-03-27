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
	
	boolean filterClosed;
	
	String caseTypeId;
	
	public CaseTypeEntityFilter(String caseTypeId) {
		this(caseTypeId, true);
	}
	
	public CaseTypeEntityFilter(String caseTypeId, boolean filterClosed) {
		this.caseTypeId = caseTypeId;
		this.filterClosed = filterClosed;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(IEntity entity) {
		CaseEntity ce = (CaseEntity)entity;
		
		return !ce.isClosed() && ce.type.equals(caseTypeId);
	}

}
