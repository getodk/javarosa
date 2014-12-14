
package org.javarosa.core.util;

import java.util.Vector;

/**
 * 
 * Only use for J2ME Compatible Vectors
 * 
 *  A SizeBoundVector that enforces that all member items be unique. You must
 *  implement the .equals() method of class E
 * 
 * @author wspride
 *
 */
public class SizeBoundUniqueVector<E> extends SizeBoundVector<E> {
	
	public SizeBoundUniqueVector(int sizeLimit) {
		super(sizeLimit);
	}

	/* (non-Javadoc)
	 * @see java.util.Vector#addElement(java.lang.Object)
	 */
	public synchronized void addElement(E obj) {
		add(obj);
	}
	
	public synchronized boolean add(E obj) {
		if(this.size() == limit) {
			additional++;
			return true;
		}
		else if(this.contains(obj)){
			return false;
		}
		else {
			super.addElement(obj);
			return true;
		}
	}
}