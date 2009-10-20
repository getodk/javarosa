/**
 * 
 */
package org.javarosa.user.api.transitions;

import org.javarosa.core.api.Transitions;
import org.javarosa.user.model.User;

/**
 * @author ctsims
 *
 */
public interface LoginStateTransitions extends Transitions {
	public void loggedIn(User u);
	
	public void exit();
}
