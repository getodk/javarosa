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

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.services.locale.Localizer;
import org.javarosa.core.services.locale.ResourceFileDataSource;

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
		locale.registerLocaleResource("default",new ResourceFileDataSource("/messages_default.txt"));
		locale.registerLocaleResource("english",new ResourceFileDataSource("/messages_en.txt"));
		locale.registerLocaleResource("swahili",new ResourceFileDataSource("/messages_sw.txt"));
		locale.registerLocaleResource("afrikaans",new ResourceFileDataSource("/messages_afr.txt"));
		
		locale.setDefaultLocale("default");
		
		locale.setLocale("english");
	}

}
