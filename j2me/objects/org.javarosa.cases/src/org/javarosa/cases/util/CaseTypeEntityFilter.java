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
package org.javarosa.cases.util;

import org.javarosa.cases.model.Case;
import org.javarosa.entity.util.EntityFilter;

/**
 * @author Clayton Sims
 * @date Mar 24, 2009 
 *
 */
public class CaseTypeEntityFilter extends EntityFilter<Case> {
	
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
	public boolean matches(Case c) {
		//OK, so I'll admit this -2 thing isn't good.
		if(userId != -2) {
			
			//Note that we don't handle any sort of adminstrative user control here.
			//If administrive users shouldn't filter cases, that should be handled elsewhere.
			
			int caseuserid = c.getUserId();
			//If the case's userid is -1 (unset), just don't worry about it.
			if(c.getUserId() != -1) {
				if(userId != caseuserid) {
					return false;
				}
			}
		}
		
		return !c.isClosed() && c.getTypeId().equals(caseTypeId);
	}

}
