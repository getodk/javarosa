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

import java.io.IOException;
import java.util.Date;

import org.javarosa.chsreferral.model.PatientReferral;
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entity.model.IEntity;

/**
 * @author Clayton Sims
 * @date Apr 26, 2009 
 *
 */
public class ReferralEntity implements IEntity {
	
	protected int recordId;
	String id;
	String type;
	Date date;
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
	public IEntity factory(int recordID) {
		ReferralEntity e = new ReferralEntity();
		e.recordId = recordID;
		return e;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#fetchRMS(org.javarosa.core.services.storage.utilities.RMSUtility)
	 */
	public Object fetch(IStorageUtility referrals) {
		return (PatientReferral)referrals.read(recordId);
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
	 * @see org.javarosa.entity.model.IEntity#getID()
	 */
	public String getID() {
		return id;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(Object o) {
		PatientReferral r = (PatientReferral)o;
		return new String[] {r.getReferralId(), r.getType(), DateUtils.formatDate(r.getDateReferred(),DateUtils.FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY)};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getName()
	 */
	public String getName() {
		return getType();
	}
	
	public String getType() { 
		return type;
	}
	
	public Date getDate() {
		return date;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getRecordID()
	 */
	public int getRecordID() {
		return recordId;
	}
	
	public boolean isPending() {
		return pending;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[] {getID(), type, DateUtils.formatDate(date, DateUtils.FORMAT_HUMAN_READABLE_DAYS_FROM_TODAY)};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#matchID(java.lang.String)
	 */
	public boolean matchID(String key) {
		String[] fields = this.getShortFields();
		for(int i = 0; i < fields.length; ++i) {
			if(fields[i].indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#matchName(java.lang.String)
	 */
	public boolean matchName(String key) {
		return matchID(key);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#readEntity(java.lang.Object)
	 */
	public void readEntity(Object o) {
		PatientReferral r = (PatientReferral)o;
		this.recordId = r.getID();
		this.type = r.getType();
		this.date = r.getDateReferred();
		this.pending = r.isPending();
	}

}
