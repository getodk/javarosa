package org.javarosa.core.model;

import org.javarosa.util.db.Persistent;

/**
 * A binding provides a link from an internal value to an external value
 * @author Clayton Sims
 *
 */
public interface IBinding extends Persistent {
	public String getId();
}
