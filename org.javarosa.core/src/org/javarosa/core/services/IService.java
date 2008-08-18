package org.javarosa.core.services;

/**
 * A Service provides access to some external source of 
 * data. 
 * 
 * This interface should be more robustly expanded
 * to cover more common ground between services in the
 * future.
 * 
 * @author Clayton Sims
 *
 */
public interface IService {
	
	/**
	 * Gets the unique name for this service
	 * 
	 * @return A unique string identifying this service.
	 */
	public String getName();
}
