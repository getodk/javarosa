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

import org.javarosa.core.services.locale.Localization;
import org.javarosa.entity.model.Entity;
import org.javarosa.user.model.User;

/**
 * @author Clayton Sims
 * @date Mar 5, 2009 
 *
 */
public class UserEntity extends Entity<User> {
	
	String username;
	String type;

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#entityType()
	 */
	public String entityType() {
		return Localization.get("user.entity.name");
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#factory(int)
	 */
	public UserEntity factory() {
		return new UserEntity();
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#readEntity(java.lang.Object)
	 */
	public void loadEntity(User u) {
		this.username = u.getUsername();
		this.type = u.getUserType();
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getHeaders(boolean)
	 */
	public String[] getHeaders(boolean detailed) {
		if(!detailed) {
			return new String[]{Localization.get("user.entity.username"),Localization.get("user.entity.type")};
		} else {
			return new String[]{Localization.get("user.entity.username"), Localization.get("user.entity.type")};
		}
	}

	public String getName() {
		return username;
	}
	
	private String getType() {
		return getType(type);
	}
	
	private String getType(String type) {
		String output = Localization.get("user.entity.unknown");
		if(User.ADMINUSER.equals(type)) {
			output = Localization.get("user.entity.admin");
		} else if(User.DEMO_USER.equals(type)) {
			output = Localization.get("user.entity.demo");
		} else if(User.STANDARD.equals(type)) {
			output = Localization.get("user.entity.normal");
		}
		return output;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getLongFields(java.lang.Object)
	 */
	public String[] getLongFields(User u) {
		String type = getType(u.getUserType());
		
		return new String[]{getName(), type};
	}

	/* (non-Javadoc)
	 * @see org.javarosa.patient.select.activity.IEntity#getShortFields()
	 */
	public String[] getShortFields() {
		return new String[]{getName(), getType()};
	}

	public boolean match(String key) {
		String[] fields = this.getShortFields();
		for(int i = 0; i < fields.length; ++i) {
			if(fields[i].indexOf(key) != -1) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getSortFields () {
		return new String[] {"NAME"};
	}
	
	public String[] getSortFieldNames () {
		return new String[] {Localization.get("user.entity.username")};
	}
	
	public Object getSortKey (String fieldKey) {
		if (fieldKey.equals("NAME")) {
			return getName();
		} else {
			throw new RuntimeException("Sort Key [" + fieldKey + "] is not supported by this entity");
		}
	}




}
