/**
 * 
 */
package org.javarosa.resources.locale;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.services.locale.Localizer;

/**
 * @author Clayton Sims
 * @date May 26, 2009 
 *
 */
public class LanguagePackModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		Localizer locale = JavaRosaServiceProvider.instance().getLocaleManager();
		locale.addAvailableLocale("default");
		locale.addAvailableLocale("english");
		locale.addAvailableLocale("swahili");
		locale.addAvailableLocale("afrikaans");
		locale.registerLocaleResource("default","/messages_default.txt");
		locale.registerLocaleResource("english","/messages_en.txt");
		locale.registerLocaleResource("swahili","/messages_sw.txt");
		locale.registerLocaleResource("afrikaans","/messages_afr.txt");
		
		locale.setDefaultLocale("default");
		
		locale.setLocale("english");
	}

}
