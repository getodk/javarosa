package org.javarosa.core.model;

import org.javarosa.core.services.storage.utilities.Externalizable;

/**
 * A binding provides a link from an internal value to an external value
 * @author Clayton Sims
 *
 */
public interface IBinding extends Externalizable {
	public String getId();
}
