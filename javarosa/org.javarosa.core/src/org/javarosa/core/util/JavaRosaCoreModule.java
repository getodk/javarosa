/**
 * 
 */
package org.javarosa.core.util;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;

/**
 * @author Clayton Sims
 * @date Jun 1, 2009 
 *
 */
public class JavaRosaCoreModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		String[] classes = {
				"org.javarosa.core.services.locale.ResourceFileDataSource",
				"org.javarosa.core.services.locale.TableLocaleSource"
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
	}
}
