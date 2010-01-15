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
	
	public void loggedIn(User u);
	
	public void exit();

}
