/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.model;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
 
public class CoreModelModule implements IModule {

	public void registerModule(Context context) {
		DataModelTreeRMSUtility dataModel = new DataModelTreeRMSUtility(DataModelTreeRMSUtility.getUtilityName());
		FormDefRMSUtility formDef = new FormDefRMSUtility(FormDefRMSUtility.getUtilityName());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(dataModel);
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(formDef);
		
		String[] classes = {
				"org.javarosa.core.model.QuestionDef",
				"org.javarosa.core.model.GroupDef",
				"org.javarosa.core.model.instance.DataModelTree",
				"org.javarosa.core.model.data.StringData",
				"org.javarosa.core.model.data.IntegerData",
				"org.javarosa.core.model.data.DecimalData",
				"org.javarosa.core.model.data.GeoPointData",
				"org.javarosa.core.model.data.SelectOneData",
				"org.javarosa.core.model.data.SelectMultiData",
				"org.javarosa.core.model.data.DateData",
				"org.javarosa.core.model.data.TimeData",
				"org.javarosa.core.model.data.PointerAnswerData",
				"org.javarosa.core.model.data.MultiPointerAnswerData",
				"org.javarosa.core.model.data.helper.BasicDataPointer"
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
	}

}
