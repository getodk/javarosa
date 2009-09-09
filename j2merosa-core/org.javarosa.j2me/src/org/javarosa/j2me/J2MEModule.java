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
package org.javarosa.j2me;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.services.storage.IStorageFactory;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.j2me.log.J2MEIncidentLogger;
import org.javarosa.j2me.storage.rms.RMSStorageUtilityIndexed;

/**
 * @author Clayton Sims
 * @date Apr 10, 2009 
 *
 */
public class J2MEModule implements IModule {

	/* (non-Javadoc)
	 * @see org.javarosa.core.api.IModule#registerModule(org.javarosa.core.Context)
	 */
	public void registerModule(Context context) {
		StorageManager.setStorageFactory(new IStorageFactory () {
			public IStorageUtility newStorage(String name, Class type) {
				return new RMSStorageUtilityIndexed(name, type);
			}
		});

		JavaRosaServiceProvider.instance().registerIncidentLogger(new J2MEIncidentLogger());
	}

}
