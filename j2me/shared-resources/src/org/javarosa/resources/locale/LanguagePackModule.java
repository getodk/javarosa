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

import org.javarosa.core.api.IModule;
import org.javarosa.core.services.locale.Localization;


/**
 * @author Clayton Sims
 * @date May 26, 2009 
 *
 */
public class LanguagePackModule implements IModule {
	
	public static final String[] locales = new String[] {"default", "en", "sw", "af", "es","fra","zh"};

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule() {
		Localization.registerLanguageFile(locales[0], "/messages_default.txt");
		Localization.registerLanguageFile(locales[1], "/messages_en.txt");
		Localization.registerLanguageFile(locales[2],"/messages_sw.txt");
		Localization.registerLanguageFile(locales[3],"/messages_afr.txt");
		Localization.registerLanguageFile(locales[4],"/messages_es.txt");
		Localization.registerLanguageFile(locales[5],"/messages_fra.txt");
		Localization.registerLanguageFile(locales[6],"/messages_zh.txt");
		
		Localization.setDefaultLocale(locales[0]);
		
		Localization.setLocale(locales[0]);
	}

}
