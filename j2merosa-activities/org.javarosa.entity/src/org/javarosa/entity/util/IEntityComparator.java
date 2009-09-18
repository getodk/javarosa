/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * @author ctsims
 *
 */
public interface IEntityComparator<E extends IEntity> {
	/**
	 * Normal Java Comparator
	 * @param e1
	 * @param e2
	 * @return -1 if e1 < e2. 0 if e1 == e2. 1 if e1 > e2
	 */
	public int compare(E e1, E e2);
	
	/**
	 * @return A human readable name for this comparison to be displayed for selection.
	 */
	public String getName();
}
