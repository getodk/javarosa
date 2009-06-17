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
package org.javarosa.user.utility;

import java.io.IOException;

import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.entity.model.IEntity;
import org.javarosa.user.model.User;

/**
 * @author Clayton Sims
 * @date Mar 5, 2009 
 *
 */
public class UserEntity implements IEntity {
	
	protected int recordID;
	
	String username;
	int userid;

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#entityType()
	 */
	public String entityType() {
		// TODO Auto-generated method stub
		return "User";
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#factory(int)
	 */
	public IEntity factory(int recordID) {
		UserEntity entity = new UserEntity();
		entity.recordID = recordID;
		return entity;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#fetchRMS(org.javarosa.core.services.storage.utilities.RMSUtility)
	 */
	public Object fetchRMS(RMSUtility rmsu) {
		User user = new User();
		try {
			rmsu.retrieveFromRMS(recordID, user);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DeserializationException e) {
			e.printStackTrace();
		}
		return user;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getHeaders(boolean)
	 */
	public String[] getHeaders(boolean detailed) {
		if(!detailed) {
			return new String[]{"Username", "ID"};
		} else {
			return new String[]{"Username", "ID", "Type"};
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getID()
	 */
	public String getID() {
		return String.valueOf(userid);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(Object o) {
		User u = (User)o;
		String type = "Unknown";
		if(User.ADMINUSER.equals(u.getUserType())) {
			type = "Administrator";
		} else if(User.DEMO_USER.equals(u.getUserType())) {
			type = "Demo";
		} else if(User.STANDARD.equals(u.getUserType())) {
			type = "Standard";
		}
		
		return new String[]{u.getUsername(), String.valueOf(u.getUserID()), type};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getName()
	 */
	public String getName() {
		return username;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getRecordID()
	 */
	public int getRecordID() {
		return recordID;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[]{username, getID()};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#matchID(java.lang.String)
	 */
	public boolean matchID(String key) {
		//TODO: I don't really understand these methods. These should be matching
		//pretty broadly, but should be reevaluated once the method contract is clear.
		String[] fields = this.getShortFields();
		for(int i = 0; i < fields.length; ++i) {
			if(fields[i].indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#matchName(java.lang.String)
	 */
	public boolean matchName(String key) {
		
		//TODO: I don't really understand these methods. These should be matching
		//pretty broadly, but should be reevaluated once the method contract is clear.
		return matchID(key);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#readEntity(java.lang.Object)
	 */
	public void readEntity(Object o) {
		User u = (User)o;
		this.username = u.getUsername();
		this.userid = u.getUserID();
	}

}
