/**
 * 
 */
package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

/**
 * @author ctsims
 *
 */
public class IDComparator implements IEntityComparator<IEntity> {
	public int compare(IEntity e1, IEntity e2) {
		return e1.getID().compareTo(e2.getID());
	}

	public String getName() {
		return "ID";
	}
}
