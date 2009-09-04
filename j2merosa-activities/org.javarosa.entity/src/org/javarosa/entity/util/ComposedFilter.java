/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * @author ctsims
 *
 */
public class ComposedFilter<E extends IEntity> implements IEntityFilter<E> {
	
	IEntityFilter<E> one, two;
	
	public ComposedFilter(IEntityFilter<E> one, IEntityFilter<E> two) {
		this.one = one;
		this.two = two;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean isPermitted(E entity) {
		return one.isPermitted(entity) && two.isPermitted(entity);
	}

}
