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
public interface EditUserTransitions extends Transitions {
	public void userEdited(User editedUser);
	
	public void cancel();
}
