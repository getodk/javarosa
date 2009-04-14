/**
 * 
 */
package org.javarosa.log;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.log.properties.LogPropertyRules;

/**
 * @author Clayton Sims
 * @date Apr 13, 2009 
 *
 */
public class LogManagementModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new LogPropertyRules());
	}

}
