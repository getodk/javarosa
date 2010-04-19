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

package org.javarosa.entity.model;

import org.javarosa.core.services.storage.EntityFilter;
import org.javarosa.core.services.storage.Persistable;

/**
 * Entity is a wrapper class around an object to be used inside the Entity Select activity. It
 * augments the base 'entity' object (case, patient, saved form, etc.) with extra methods that
 * govern its appearance and behavior inside the activity. For example: info columns and their
 * content, sorting, filtering, styling, etc.
 * 
 * Note that this base Entity class is abstract. You have to provide some base functionality
 * in your specific entity (reading and caching fields from the object, determining the columns
 * to display and their contents, etc.), while other functionality has defaults provided here
 * that need only be overridden if desired (filtering, sorting, etc.).
 * 
 * @author Drew Roos
 *
 * @param <E> underlying entity type
 */
public abstract class Entity <E extends Persistable> {
	
	/**
	 * All selectable entities must implement persistable, as the activity iterates over a
	 * designated StorageUtility, and the activity returns only record IDs, not the entity
	 * itself. This field caches the entity's record ID.
	 */
	private int recordID;
	
	/**
	 * Your specific entity implementation should cache other fields from the base object <E>
	 */
	
	/**
	 * Factory method
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @return a fresh instance of this exact same type of Entity (i.e., if you subclass
	 * Entity (which you must, since Entity is abstract) this method must return an instance
	 * of the same subclass!)
	 */
	public abstract Entity<E> factory ();

	/**
	 * Read in the entity (fresh from the StorageUtility) and cache relevant fields for display
	 * in the list view of the activity. Record ID is cached automatically; the rest is delegated
	 * out to loadEntity.
	 * 
	 * @param e
	 */
	public final void readEntity (E e) {
		this.recordID = e.getID();
		loadEntity(e);
	}
	
	/**
	 * Cache certain fields from the entity. See readEntity()
	 * 
	 * @param e
	 */
	protected abstract void loadEntity (E e);
	
	/**
	 * Return the human-readable name of this entity, for use in 'Add new xxx?'-type dialogs
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @return name of this entity's type
	 */
	public abstract String entityType ();
	
	/**
	 * Return this entity's record ID
	 * 
	 * @return record ID
	 */
	public final int getRecordID () {
		return recordID;
	}
	
	/**
	 * Match this entity against a search string. Searching semantics are completely up to the entity
	 * 
	 * @param key search key; will always have non-zero length
	 * @return true if this entity matches the key
	 */
	public boolean match (String key) {
		return true;
	}	

	/**
	 * Return the human-readable titles of the headers for this entity, in the order they should be
	 * displayed
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @param detailed if true, return headers for data shown on the 'detail' screen; if false, return
	 *   headers for data shown on the abbreviated 'list view' screen
	 * @return headers
	 */
	public abstract String[] getHeaders (boolean detailed);
	
	public int[] getStyleHints (boolean header) {
		int[] empty = new int[this.getHeaders(false).length];
		for(int i = 0; i < empty.length ; ++i ) {
			empty[i] = -1;
		}
		return empty;
	}
	
	/**
	 * @param header Whether the forms pertain to the header or to the
	 * fields themselves
	 * @return A set of hints representing how the text inside of the field
	 * should be displayed. The basic forms are
	 * <ul><li>null : normal text</li><li>"image" : an image</li></ul>
	 */
	public String[] getForms(boolean header) {
		return new String[this.getHeaders(false).length];
	}

	/**
	 * Return the data to be shown for this entity on the list screen. Data should correspond one-for-one
	 * with the headers from getHeaders(false)
	 * 
	 * @return data to display
	 */
	public abstract String[] getShortFields ();

	/**
	 * Return the data to be shown for this entity on the detail screen. Data should correspond one-for-one
	 * with the headers from getHeaders(true)
	 * 
	 * It is likely that we want to display here more information than we cached for use on the list screen.
	 * Therefore, the underlying entity object is provided, so you can extract additional data. You may still
	 * access the cached fields, though.
	 * 
	 * @param e underlying entity object
	 * @return data to display 
	 */	
	public abstract String[] getLongFields (E e);
	
	/**
	 * Return a list of text keys that identify the various fields by which this entity may be sorted.
	 * If more than one key is provided, the user will be able to choose between the different sort fields.
	 * The first key listed will be used by default. If no keys are returned (null or empty array), the
	 * ordering will be undefined.
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @return list of keys for sort fields
	 */
	public String[] getSortFields () {
		return new String[] {"DEFAULT"};
	}
	
	/**
	 * Return the human-readable title for each available sort field, corresponding one-for-one with
	 * getSortFields()
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @return list of sort field titles
	 */
	public String[] getSortFieldNames () {
		return new String[] {"Record ID"};
	}
		
	/**
	 * Return this entity's sort key for the given sort field. This is the value that will determine
	 * the entity's ordering if this sort field is selected by the activity. If the provided field does
	 * not appear in the list from getSortFields, an exception should be thrown.
	 * 
	 * The returned key should be of type Integer, Long, Double, String, or Date. Or null if not applicable
	 * (entities with null keys will be placed at the end). Other types are not comparable and will throw
	 * an exception. This method should always return the same type for a given sort field, mixing types
	 * within the same sort field will throw an exception.
	 * 
	 * @param fieldKey active sort field
	 * @return sort key
	 * @throws RuntimeException if fieldKey is not in the list of available sort fields
	 * @throws ClassCastException if a consistent type is not always returned for the same fieldKey
	 */
	public Object getSortKey (String fieldKey) {
		if (fieldKey.equals("DEFAULT")) {
			return new Integer(recordID);
		} else {
			throw new RuntimeException("Sort Key [" + fieldKey + "] is not supported by this entity");
		}
	}
	
	/**
	 * Get the entity filter. Only entities that satisfy the filter will be included in the activity.
	 * Returning null means no filtering will be done.
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @return filter
	 */
	public EntityFilter<? super E> getFilter () {
		return null;
	}
	
	/**
	 * Get custom styling key (needed to support differing # of columns among different types of Entity)
	 * and in general make the activity look good. Returning null means no custom styling will be done
	 * (warning: activity will likely look horrible!)
	 * 
	 * Note: this is essentially a static method
	 * 
	 * @return return a custom J2ME Polish styling key for this entity
	 */
	public String getStyleKey () {
		return null;
	}
}
