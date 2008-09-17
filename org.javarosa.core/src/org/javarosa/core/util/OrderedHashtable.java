/* -----------------------------------------------------------------------------
 * SimpleOrderedHashtable.java
 * Author: C. Enrique Ortiz
 * Copyright (c) 2004-2005 C. Enrique Ortiz <eortiz@j2medeveloper.com>
 *
 * SimpleOrderedHashtable.java implements a simple Hashtable that is
 * chronologically ordered.
 *
 * This is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any
 * later version.
 *
 * Usage & redistributions of source code must retain the above copyright notice.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should get a copy of the GNU Lesser General Public License from
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * -----------------------------------------------------------------------------
 */
package org.javarosa.core.util;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 *  Implements an Ordered Hashtable, with elements in
 *    chronological order (i.e. insertion order)
 */
public class OrderedHashtable extends Hashtable {
    private Vector orderedKeys;

    /**
     *  Constructor, creates an SimpleOrderedHashtable.
     */
    public OrderedHashtable() {
    	super();
    	orderedKeys = new Vector();
    }

    /**
     *  Constructor, creates an SimpleOrderedHashtable.
     *  @param initialCapacity is the initial size for the container.
     */
    public OrderedHashtable(int initialCapacity) {
    	super(initialCapacity);
    	orderedKeys = new Vector(initialCapacity);
    }

    /**
     *  Clears this SimpleOrderedHashtable so that it contains no keys.
     */
    synchronized public void clear() {
        orderedKeys.removeAllElements();
        super.clear();
    }
    
    /**
     *  Returns the component at the specified index. Not in Hashtable.
     *  @param index is an index into this SimpleOrderedHashtable.
     *  @return the <code>Object</code> component at the specified index.
     *  @throws ArrayIndexOutOfBoundsException if index is out of bounds.
     */
    synchronized public Object elementAt(int index) {
        return get(keyAt(index));
    }
    
    /**
     *  Returns an enumeration of the elements in this SimpleOrderedHashtable.
     *  @return an enumeration of the elements in this SimpleOrderedHashtable.
     */
    synchronized public Enumeration elements() {
        Vector elements = new Vector();
        for (int i = 0; i < size(); i++) {
            elements.addElement(elementAt(i));
        }
        return elements.elements();
    }
    
    /**
     *  Returns the index of the specified <code>Object</code>. Not in Hashtable.
     *  @param key is a key in the SimpleOrderedHashtable.
     *  @return the index of the specified <code>Object</code>.
     */
    synchronized public int indexOfKey (Object key) {
        return orderedKeys.indexOf(key);
    }
    
    /**
     *  Returns the key at the specified index. Not in Hashtable.
     *  @param index is an index into this SimpleOrderedHashtable.
     *  @return the <code>Object</code> key at the specified index.
     *  @throws ArrayIndexOutOfBoundsException if index is out of bounds.
     */
    synchronized public Object keyAt(int index) {
        return orderedKeys.elementAt(index);
    }
    
    /**
     *  Returns an enumeration of the keys in this SimpleOrderedHashtable.
     *  @return an enumeration of the keys in this SimpleOrderedHashtable.
     */
    synchronized public Enumeration keys() {
        return orderedKeys.elements();
    }
    
    /**
     *  Maps the specified key to the specified value in this SimpleOrderedHashtable.
     *  The value can be retrieved by calling the get method with a key that is
     *  equal to the original key.
     *  @param key is the hashtable key.
     *  @param value is the value.
     *  @return the previous value of the specified key in this
     *  SimpleOrderedHashtable, or null if it did not have one.
     */
    synchronized public Object put(Object key, Object value) {
    	if (key == null) {
    		throw new NullPointerException();
    	}
    	
        int i = orderedKeys.indexOf(key);
        if (i == -1)  {
            //  Add new name/value pair.
            orderedKeys.addElement(key); // insert (append) to the end of the list
        } else {
            //  Replace name/value pair.
            orderedKeys.setElementAt(key, i);
        }
        return super.put(key, value);
    }

    /**
     *  Removes the key (and its corresponding value) from this hashtable. This
     *  method does nothing if the key is not in the hashtable.
     *  @param key is the key that needs to be removed.
     */
    synchronized public Object remove(Object key) {
        orderedKeys.removeElement(key);
        return super.remove(key);
    }
    
    /**
     * Removes an element at the specified index. Not in Hashtable.
     * @param i is the index of the element to remove.
     */
    synchronized public void removeAt(int i) {
        remove(keyAt(i));
        orderedKeys.removeElementAt(i);
    }
    
    public String toString () {
    	StringBuffer sb = new StringBuffer();
    	sb.append("[");
    	for (Enumeration e = keys(); e.hasMoreElements(); ) {
    		Object key = e.nextElement();
    		sb.append(key.toString());
    		sb.append(" => ");
    		sb.append(get(key).toString());
    		if (e.hasMoreElements())
    			sb.append(", ");
    	}
    	sb.append("]");    	
    	return sb.toString();
    }
}