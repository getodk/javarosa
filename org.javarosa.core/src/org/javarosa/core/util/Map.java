package org.javarosa.core.util;

import java.util.Vector;

/**
 * A Map is a data object that maintains a map from one set of data
 * objects to another. This data object is superior to a Hashtable
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