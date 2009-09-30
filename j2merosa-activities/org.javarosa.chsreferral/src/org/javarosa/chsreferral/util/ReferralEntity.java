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
package org.javarosa.chsreferral.util;

import java.util.Date;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.entity.model.Entity;

/**
 * @author Clayton Sims
 * @date Apr 26, 2009 
 *
 */
public class ReferralEntity extends Entity<PatientReferral> {
	
	String id;
	protected String type;
	protected Date dateCreated;
	protected Date dateDue;
	boolean pending;
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#entityType()
	 */
	public String entityType() {
		return "Referral";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#factory(int)
	 */
	public ReferralEntity factory() {
		return new ReferralEntity();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#readEntity(java.lang.Object)
	 */
	public void loadEntity (PatientReferral r) {
		this.id = r.getReferralId();
		this.type = r.getType();
		this.dateCreated = r.getDateCreated();
		this.dateDue = r.getDateDue();
		this.pending = r.isPending();
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getHeaders(boolean)
	 */
	public String[] getHeaders(boolean detailed) {
		if(detailed) {
			return new String[] {"ID", "Type", "Date"};
		} else {
			return new String[] {"ID", "Type", "Date"};
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[] {id, type, DateUtils.formatDate(dateCreated, DateUtils.FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY)};
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(PatientReferral r) {
		return getShortFields();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#matchID(java.lang.String)
	 */
	public boolean match (String key) {
		String[] fields = this.getShortFields();
		for(int i = 0; i < fields.length; ++i) {
			if(fields[i].indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}
	
	public String getType() { 
		return type;
	}
	
	public Date getDateCreated() {
		return dateCreated;
	}
	
	public Date getDateDue() {
		return dateDue;
	}
	
	public boolean isPending() {
		return pending;
	}
	
	public String getID() {
		return id;
	}
}
