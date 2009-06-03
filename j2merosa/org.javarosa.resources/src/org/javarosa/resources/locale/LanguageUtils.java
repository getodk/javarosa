/**
 * 
 */
package org.javarosa.resources.locale;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.properties.JavaRosaPropertyRules;
import org.javarosa.core.util.PropertyUtils;

/**
 * @author Clayton Sims
 * @date Jun 3, 2009 
 *
 */
public class LanguageUtils {
	/**
	 * Initializes the language for the application in three steps 
	 * 
	 *  1. If the language property exists, fetch it and set the current locale
	 *  2. Otherwise, set the default language to the preprocessed value 
	 *  "javarosa.locale.default" if that preprocessed value exists
	 *  3. If the value does not exist, use the provided string to set the
	 *  application's default language
	 *  
	 * @param fallbackDefaultLanguage The language used as a fallback if none
	 * can be fetched from the preprocessed value
	 */
	public static void initializeLanguage(String fallbackDefaultLanguage) {
		//#ifdef javarosa.locale.default:defined
		//#= JavaRosaServiceProvider.instance().getLocaleManager().setLocale(PropertyUtils.initializeProperty(JavaRosaPropertyRules.CURRENT_LOCALE, "${javarosa.locale.default}"));
		//#else
		JavaRosaServiceProvider.instance().getLocaleManager().setLocale(
				PropertyUtils.initializeProperty(JavaRosaPropertyRules.CURRENT_LOCALE, fallbackDefaultLanguage));
		//#endif
	}
}
