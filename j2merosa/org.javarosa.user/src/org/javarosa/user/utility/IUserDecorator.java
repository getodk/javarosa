/**
 * 
 */
package org.javarosa.user.utility;

/**
 * @author Clayton Sims
 * @date Mar 3, 2009 
 *
 */
public interface IUserDecorator {
	public String[] getPertinentProperties();
	
	/**
	 * @param property The property to be decorated
	 * @return a human readable name that describes the presented property. 
	 */
	public String getHumanName(String property);
}
