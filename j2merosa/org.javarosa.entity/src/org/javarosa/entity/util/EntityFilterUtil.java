/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * Entity Filter static utility methods, because Java won't allow them to be defined
 * in the interface
 * 
 * @author Clayton Sims
 * @date May 29, 2009 
 *
 */
public class EntityFilterUtil {
	public static IEntityFilter stack(final IEntityFilter one, final IEntityFilter two) {
		 return new IEntityFilter() {
			public boolean isPermitted(IEntity entity) {
				return one.isPermitted(entity) && two.isPermitted(entity);
			}
		 };
	}
}
