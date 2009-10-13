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
package org.javarosa.entity.util;

import org.javarosa.core.Context;
import org.javarosa.core.services.storage.utilities.RMSUtility;
import org.javarosa.entity.model.IEntity;

/**
 * @author Clayton Sims
 * @date Mar 20, 2009 
 *
 */
public class EntitySelectContext extends Context {
	public static final String ENTITY_RMS_KEY = "entity-rms";
	public static final String ENTITY_PROTO_KEY = "entity-type";
	public static final String NEW_ENTITY_ID_KEY_KEY = "new-entity-key";
	public static final String NEW_MODE_KEY = "new-mode-key";
	public static final String ENTITY_FILTER_KEY = "esc_filter_key";
	public static final String BAIL_EMPTY = "esc_exit_empty";
	public static final String ENTITY_STYLE_KEY = "esc_style_key";

	
	public EntitySelectContext(Context c) {
		super(c);
	}
	
	public void setRMSUtility(RMSUtility util) {
		setElement(ENTITY_RMS_KEY, util);
	} 
	
	public RMSUtility getRMSUtility() {
		return (RMSUtility)getElement(ENTITY_RMS_KEY);
	}
	
	public void setEntityProtoype(IEntity entity) {
		setElement(ENTITY_PROTO_KEY, entity);
	}
	
	public IEntity getEntityPrototype() {
		return (IEntity)getElement(ENTITY_PROTO_KEY);
	}
	
	public void setNewEntityIDKey(String key) {
		setElement(NEW_ENTITY_ID_KEY_KEY, key);
	}
	
	public String getNewEntityIDKey() {
		return (String)getElement(NEW_ENTITY_ID_KEY_KEY);
	}
	
	public void setNewMode(Integer newMode) {
		setElement(NEW_MODE_KEY, newMode);
	}
	
	public Integer getNewMode() {
		return (Integer)getElement(NEW_MODE_KEY);
	}
	
	public void setStyleKey(String key) {
		setElement(ENTITY_STYLE_KEY, key);
	}
	
	public String getStyleKey() {
		return (String)getElement(ENTITY_STYLE_KEY);
	}
	
	public void setBailOnEmpty(boolean bail) {
		setElement(BAIL_EMPTY, new Boolean(bail));
	}
	
	public boolean isBailOnEmpty() {
		Boolean bail = (Boolean)getElement(BAIL_EMPTY);
		if(bail == null) {
			return false;
		} else {
			return bail.booleanValue();
		}
	}
	
	public void setEntityFilter(IEntityFilter filter) {
		setElement(ENTITY_FILTER_KEY, filter);
	}
	
	public IEntityFilter getEntityFilter() {
		IEntityFilter filter = (IEntityFilter)getElement(ENTITY_FILTER_KEY);
		if(filter == null) {
			return new AcceptAllFilter();
		} else {
			return filter;
		}
	}
}