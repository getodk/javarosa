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
public interface AddUserTransitions extends Transitions {
	public void userAdded(User newUser);
	public void cancel();
}
