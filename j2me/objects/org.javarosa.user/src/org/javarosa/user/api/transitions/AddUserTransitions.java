/**
 * 
 */
package org.javarosa.user.api.transitions;

import org.javarosa.user.model.User;

/**
 * @author ctsims
 *
 */
public interface AddUserTransitions {
	public void userAdded(User newUser);
	public void cancel();
}
