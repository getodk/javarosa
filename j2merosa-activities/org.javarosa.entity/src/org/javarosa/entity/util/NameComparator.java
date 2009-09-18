/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * @author ctsims
 *
 */
public class NameComparator implements IEntityComparator<IEntity> {

	public int compare(IEntity e1, IEntity e2) {
		return e1.getName().compareTo(e2.getName());
	}

	public String getName() {
		return "Name";
	}

}
