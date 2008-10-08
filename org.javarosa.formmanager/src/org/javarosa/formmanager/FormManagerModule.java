package org.javarosa.formmanager;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.formmanager.properties.FormManagerProperties;

public class FormManagerModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule()
	 */
	public void registerModule(Context context) {
		JavaRosaServiceProvider.instance().getPropertyManager().addRules(new FormManagerProperties());
		PropertyUtils.initializeProperty(FormManagerProperties.VIEW_TYPE_PROPERTY, FormManagerProperties.VIEW_CHATTERBOX);
	}

}
