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
	
	int userId= -2;
	
	public CaseTypeEntityFilter(String caseTypeId) {
		this(caseTypeId, true);
	}
	
	public CaseTypeEntityFilter(String caseTypeId, boolean filterClosed) {
		this.caseTypeId = caseTypeId;
		this.filterClosed = filterClosed;
	}
	
	public void filterForUserId(int userId) {
		this.userId = userId;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(IEntity entity) {
		CaseEntity ce = (CaseEntity)entity;
		
		//OK, so I'll admit this -2 thing isn't good.
		if(userId != -2) {
			
			//Note that we don't handle any sort of adminstrative user control here.
			//If administrive users shouldn't filter cases, that should be handled elsewhere.
			
			int caseuserid = ce.getUserId();
			//If the case's userid is -1 (unset), just don't worry about it.
			if(ce.getUserId() != -1) {
				if(userId != caseuserid) {
					return false;
				}
			}
		}
		
		return !ce.isClosed() && ce.type.equals(caseTypeId);
	}

}
