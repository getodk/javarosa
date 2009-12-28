/**
 * 
 */
package org.javarosa.entity.util;

import java.util.Hashtable;


/**
 * @author ctsims
 *
 */
public class StackedEntityFilter<E> extends EntityFilter<E> {
	
	EntityFilter<E> one, two;
	
	public StackedEntityFilter(EntityFilter<E> one, EntityFilter<E> two) {
		this.one = one;
		this.two = two;
	}

	public boolean preFilter (int id, Hashtable<String, Object> metaData) {
		return one.preFilter(id, metaData) && two.preFilter(id, metaData);
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean matches(E entity) {
		return one.matches(entity) && two.matches(entity);
	}

}
