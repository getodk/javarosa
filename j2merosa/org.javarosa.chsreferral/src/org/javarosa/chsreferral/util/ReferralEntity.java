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
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entity.model.IEntity;
import org.javarosa.xpath.expr.XPathFuncExpr;

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
	public Object fetchRMS(RMSUtility rmsu) {
		PatientReferral r = new PatientReferral();
		try {
			rmsu.retrieveFromRMS(recordId, r);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializationException e) {
			e.printStackTrace();
		}
		return r;
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
		return new String[] {r.getReferralId(), r.getType(), getDaysAgo(r.getDateReferred())};
	}
	
	protected String getDaysAgo(Date d) {
		String daysAgoStr;
		int daysAgo = (int)(XPathFuncExpr.toNumeric(new Date()).doubleValue() - XPathFuncExpr.toNumeric(d).doubleValue());
		//#if commcare.lang.sw
		daysAgoStr = (daysAgo < 0 ? "From the futurrrrrre" : daysAgo == 0 ? "Leo" : daysAgo == 1 ? "Jana" : daysAgo == 2 ? "Juzi" : daysAgo + " days ago");
		//#else
		daysAgoStr = (daysAgo < 0 ? "From the futurrrrrre" : daysAgo == 0 ? "Today" : daysAgo == 1 ? "Yesterday" : daysAgo + " days ago");
		//#endif
		return daysAgoStr;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
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
		return new String[] {getID(), type, getDaysAgo(date)};
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
		this.recordId = r.getRecordId();
		this.type = r.getType();
		this.date = r.getDateReferred();
		this.pending = r.isPending();
	}

}
