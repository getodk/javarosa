/**
 * 
 */
package org.javarosa.user.api.transitions;

import org.javarosa.user.model.User;

/**
 * Transitions for a user interface for creating a new user object.
 * 
 * Note, the user will not actually be added to RMS upon successful creation,
 * but must be added afterwards.
 * 
 * @author ctsims
 *
 */
public interface AddUserTransitions {
	public void userCreated(User newUser);
	public void cancel();
}
