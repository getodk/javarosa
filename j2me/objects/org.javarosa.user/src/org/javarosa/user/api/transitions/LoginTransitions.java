/**
 * 
 */
package org.javarosa.user.api.transitions;

import org.javarosa.user.model.User;

/**
 * @author ctsims
 *
 */
public interface LoginTransitions {
	
	/**
	 * Signal that the provided user logged in, and provide the secret provided
	 * (in case it should be processed to create sync tokens, etc). The password
	 * _should not be stored into memory_. 
	 * 
	 * @param u The User who successfully authenticated
	 * @param password The secret used
	 */
	public void loggedIn(User u, String password);
	
	public void exit();

}
