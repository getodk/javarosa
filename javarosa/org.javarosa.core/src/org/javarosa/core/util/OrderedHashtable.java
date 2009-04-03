package org.javarosa.core.util;

import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;

public class OrderedHashtable extends Hashtable {
    private Vector orderedKeys;

    public OrderedHashtable() {
    	super();
    	orderedKeys = new Vector();
    }

    public OrderedHashtable(int initialCapacity) {
    	super(initialCapacity);
    	orderedKeys = new Vector(initialCapacity);
    }

    public void clear() {
        orderedKeys.removeAllElements();
        super.clear();
    }
    
    public Object elementAt(int index) {
        return get(keyAt(index));
    }
    
    public Enumeration elements() {
        Vector elements = new Vector();
        for (int i = 0; i < size(); i++) {
            elements.addElement(elementAt(i));
        }
        return elements.elements();
    }
    
    public int indexOfKey (Object key) {
        return orderedKeys.indexOf(key);
    }
    
    public Object keyAt(int index) {
        return orderedKeys.elementAt(index);
    }
    
    public Enumeration keys() {
        return orderedKeys.elements();
    }
    
    public Object put(Object key, Object value) {
    	if (key == null) {
    		throw new NullPointerException();
    	}
    	
        int i = orderedKeys.indexOf(key);
        if (i == -1)  {
            orderedKeys.addElement(key); 
        } else {
            orderedKeys.setElementAt(key, i);
        }
        return super.put(key, value);
    }

    public Object remove(Object key) {
        orderedKeys.removeElement(key);
        return super.remove(key);
    }
    
    public void removeAt(int i) {
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