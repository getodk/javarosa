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
import org.javarosa.core.model.utils.DateUtils;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.entity.model.Entity;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CaseEntity extends Entity<Case> {
	protected String name;
	protected String id;
	protected String type;
	protected int userId;
	protected boolean closed;

	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#entityType()
	 */
	public String entityType() {
		return "Case";
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#factory(int)
	 */
	public CaseEntity factory() {
		return new CaseEntity();
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getHeaders(boolean)
	 */
	public String[] getHeaders(boolean detailed) {
		String [] headers;
		if(detailed) {
			//#if commcare.lang.sw
			//headers = new String[] {"Jina", "Namba", "Tarehe ya mkutano wa kwanza", "Bado inaendelea?"};
			headers = new String[] {"Jina", "Namba", "Tar.", "Bado inaendelea?"};
			//#else
			headers = new String[] {"Name", "ID", "Date Opened", "Currently Open"};			
			//#endif
		} else {
			//#if commcare.lang.sw
			headers = new String[] {"Jina", "Namba"};
			//#else
			headers = new String[] {"Name", "ID"};
			//#endif
		}
		return headers;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(Case c) {
		String date;
		if(c.getDateOpened() == null) {
			date = "unknown";
		} else {
			date = DateUtils.formatDate(c.getDateOpened(), DateUtils.FORMAT_HUMAN_READABLE_SHORT);
		}

		String open;
		if(c.isClosed()) {
			//#if commcare.lang.sw
			open = "Hapana";
			//#else
			open = "No";
			//#endif
		} else {
			//#if commcare.lang.sw
			open = "Ndiyo";
			//#else
			open = "Yes";
			//#endif
		}
		return new String[] {c.getName(), ExtUtil.emptyIfNull((String)c.getProperty("case-id")), date, open};
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[] {this.getName(), this.getID()};
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#matchID(java.lang.String)
	 */
	public boolean match (String key) {
		String[] fields = this.getShortFields();
		for(int i = 0; i < fields.length; ++i) {
			if(fields[i].startsWith(key)) {
				return true;
			}
		}
		return false;
	}
		
	public String[] getSortFields () {
		return new String[] {"NAME", "ID"};
	}
	
	public String[] getSortFieldNames () {
		return new String[] {"Name", "Case ID"};
	}
	
	public Object getSortKey (String fieldKey) {
		if (fieldKey.equals("NAME")) {
			return getName();
		} else if (fieldKey.equals("ID")) {
			return getID();
		} else {
			throw new RuntimeException("Sort Key [" + fieldKey + "] is not supported by this entity");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.model.IEntity#readEntity(java.lang.Object)
	 */
	public void loadEntity(Case c) {
		this.name = c.getName();
		this.id = ExtUtil.emptyIfNull((String)c.getProperty("case-id"));
		this.type = c.getTypeId();
		this.closed = c.isClosed();
		this.userId = c.getUserId();
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isClosed() {
		return closed;
	}
	
	public int getUserId() {
		return userId;
	}

	public String getID() {
		return id;
	}
}
