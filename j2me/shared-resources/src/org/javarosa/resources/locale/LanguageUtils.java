/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * 
 */
package org.javarosa.resources.locale;

import org.javarosa.core.services.locale.Localization;
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
	 *  2. Otherwise, if the useProperty argument is true, set the default language 
	 *  to the preprocessed value "javarosa.locale.default" if that preprocessed value exists
	 *  3. If the value does not exist, use the provided string to set the
	 *  application's default language
	 *  
	 * @param useProperty Whether to attempt to pull the default language property
	 * @param fallbackDefaultLanguage The language used as a fallback if none
	 * can be fetched from the preprocessed value
	 */
	public static void initializeLanguage(boolean useProperty, String fallbackDefaultLanguage) {
		if(useProperty) {
		//#ifdef javarosa.locale.default:defined
	    //#= Localization.setLocale(PropertyUtils.initializeProperty(JavaRosaPropertyRules.CURRENT_LOCALE, "${javarosa.locale.default}"));
		//#else
		Localization.setLocale(
				PropertyUtils.initializeProperty(JavaRosaPropertyRules.CURRENT_LOCALE, fallbackDefaultLanguage));
		//#endif
		} else {
			Localization.setLocale(
					PropertyUtils.initializeProperty(JavaRosaPropertyRules.CURRENT_LOCALE, fallbackDefaultLanguage));
		}
	}
}
