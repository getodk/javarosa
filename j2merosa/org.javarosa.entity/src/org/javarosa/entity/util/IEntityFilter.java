package org.javarosa.entity.util;

import org.javarosa.entity.model.IEntity;

public interface IEntityFilter {
	public boolean isPermitted(IEntity entity);
}
