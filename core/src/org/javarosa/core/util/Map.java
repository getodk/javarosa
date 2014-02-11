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

package org.javarosa.core.util;

import java.util.Enumeration;
import java.util.Vector;

/**
 * A Map is a data object that maintains a map from one set of data
 * objects to another. This data object is superior to a HashMap
 * in instances where O(1) lookups are not a priority, due to its
 * smaller memory footprint.
 * 
 * Lookups in a map are accomplished in O(n) time.
 * 
 * @author Clayton Sims
 *
 */
public class Map {
	Vector keys = new Vector();
	Vector elements = new Vector();
	
	/**
	 * Places the key/value pair in this map. Any existing
	 * mapping keyed by the key parameter is removed.
	 * 
	 * @param key
	 * @param value
	 */
	public void put(Object key, Object value) {
		if(containsKey(key)) {
			remove(key);
		}
		keys.addElement(key);
		elements.addElement(value);
	}
	
	public Enumeration keys(){
		return keys.elements();
	}
	
	public int size() {
		return keys.size();
	}
	
	/**
	 * @param key
	 * @return The object bound to the given key, if one exists. 
	 * null otherwise.
	 */
	public Object get(Object key) {
		int index = getIndex(key);
		if(index == -1) {
			return null;
		}
		return elements.elementAt(index);
	}
	
	/**
	 * Removes any mapping from the given key 
	 * @param key
	 */
	public void remove(Object key) {
		int index = getIndex(key);
		if(index == -1 ) {
			return;
		}
		keys.removeElementAt(index);
		elements.removeElementAt(index);
		if(keys.size() != elements.size()) {
			//This is _really bad_,
		}
	}
	
	/**
	 * Removes all keys and values from this map.
	 */
	public void clear() {
		keys.removeAllElements();
		elements.removeAllElements();
	}
	
	/**
	 * Whether or not the key is bound in this map
	 * @param key 
	 * @return True if there is an object bound to the given
	 * key in this map. False otherwise.
	 */
	public boolean containsKey(Object key) {
		return getIndex(key) != -1;
	}
	
	private int getIndex(Object key) {
		for(int i = 0; i < keys.size() ; ++i) {
			if(keys.elementAt(i).equals(key)) {
				return i;
			}
		}
		return -1;
	}
}