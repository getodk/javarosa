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

package org.javarosa.formmanager;

import org.javarosa.core.api.IModule;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.PropertyUtils;
import org.javarosa.formmanager.properties.FormManagerProperties;

public class FormManagerModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule()
	 */
	public void registerModule() {
		PropertyManager._().addRules(new FormManagerProperties());
		PropertyUtils.initializeProperty(FormManagerProperties.VIEW_TYPE_PROPERTY, FormManagerProperties.VIEW_CHATTERBOX);
		PropertyUtils.initializeProperty(FormManagerProperties.USE_HASH_FOR_AUDIO_PLAYBACK, FormManagerProperties.HASH_AUDIO_PLAYBACK_YES);
	}

}
