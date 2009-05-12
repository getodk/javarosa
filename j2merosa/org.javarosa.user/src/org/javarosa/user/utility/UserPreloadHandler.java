/**
 * 
 */
package org.javarosa.user.utility;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.user.model.User;

/**
 * @author Clayton Sims
 * @date May 12, 2009 
 *
 */
public class UserPreloadHandler implements IPreloadHandler {
	User user;
	
	public UserPreloadHandler(User user) {
		this.user = user;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePostProcess(org.javarosa.core.model.instance.TreeElement, java.lang.String)
	 */
	public boolean handlePostProcess(TreeElement node, String params) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePreload(java.lang.String)
	 */
	public IAnswerData handlePreload(String preloadParams) {
		Object property = user.getProperty(preloadParams);
		if(property instanceof String) {
			return new StringData((String)property);
		} else {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "user";
	}

}
