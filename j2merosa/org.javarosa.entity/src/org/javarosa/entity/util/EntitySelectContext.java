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