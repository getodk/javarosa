package org.javarosa.core.util;

/**
 * Observation interface for the observer
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public interface Observer {

	/**
	 * @param observable
	 * @param arg
	 */
	public void update(Observable observable, Object arg);

}
