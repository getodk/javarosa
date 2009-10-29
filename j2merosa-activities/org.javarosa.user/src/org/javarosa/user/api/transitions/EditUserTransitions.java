/**
 * 
 */
package org.javarosa.user.api.transitions;

import org.javarosa.user.model.User;

/**
 * @author ctsims
 *
 */
public interface EditUserTransitions {
	public void userEdited(User editedUser);
	
	public void cancel();
}
