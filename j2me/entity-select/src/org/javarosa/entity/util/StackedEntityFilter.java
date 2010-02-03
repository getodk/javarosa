/**
 * 
 */
package org.javarosa.entity.util;

import java.util.Hashtable;

import org.javarosa.core.services.storage.EntityFilter;


/**
 * @author ctsims
 *
 */
public class StackedEntityFilter<E> extends EntityFilter<E> {
	
	public static final int OP_AND = 0;
	public static final int OP_OR = 1;
	
	EntityFilter<E> one, two;
	int op;
	
	public StackedEntityFilter(EntityFilter<E> one, EntityFilter<E> two) {
		this(one, two, OP_AND);
	}
		
	public StackedEntityFilter(EntityFilter<E> one, EntityFilter<E> two, int operator) {
		this.one = one;
		this.two = two;
		this.op = op;
	}

	public int preFilter (int id, Hashtable<String, Object> metaData) {
		switch (one.preFilter(id, metaData)) {
		case PREFILTER_INCLUDE:
			if (op == OP_AND) {
				return two.preFilter(id, metaData);
			} else {
				return PREFILTER_INCLUDE;
			}
		case PREFILTER_EXCLUDE:
			if (op == OP_AND) {
				return PREFILTER_EXCLUDE;
			} else {
				return two.preFilter(id, metaData);
			}
		case PREFILTER_FILTER:
			int pf2 = two.preFilter(id, metaData);
			if (op == OP_AND) {
				return (pf2 == PREFILTER_EXCLUDE ? PREFILTER_EXCLUDE : PREFILTER_FILTER);
			} else {
				return (pf2 == PREFILTER_INCLUDE ? PREFILTER_INCLUDE : PREFILTER_FILTER);
			}
		default:
			throw new RuntimeException();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.entity.util.IEntityFilter#isPermitted(org.javarosa.entity.model.IEntity)
	 */
	public boolean matches(E entity) {
		if (one.matches(entity)) {
			if (op == OP_AND) {
				return two.matches(entity);
			} else {
				return true;
			}
		} else {
			if (op == OP_AND) {
				return false;
			} else {
				return two.matches(entity);
			}
		}
	}

}
