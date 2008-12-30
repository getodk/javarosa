package org.javarosa.core.model;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.instance.DataModelTree;
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
