/**
 * 
 */
package org.javarosa.entity.util;


/**
 * @author ctsims
 *
 */
public class StackedEntityFilter<E> implements IEntityFilter<E> {
	
	IEntityFilter<E> one, two;
	
	public StackedEntityFilter(IEntityFilter<E> one, IEntityFilter<E> two) {
		this.one = one;
		this.two = two;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean matches(E entity) {
		return one.matches(entity) && two.matches(entity);
	}

}
