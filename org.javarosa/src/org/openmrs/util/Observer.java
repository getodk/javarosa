package org.openmrs.util;

/**
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public interface Observer {

	/**
	 * @param observable
	 * @param arg
	 */
	public void update(Observable observable, Object arg);

}
