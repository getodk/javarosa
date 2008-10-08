package org.javarosa.core.model;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;

public class CoreModelModule implements IModule {

	public void registerModule(Context context) {
		DataModelTreeRMSUtility dataModel = new DataModelTreeRMSUtility(DataModelTreeRMSUtility.getUtilityName());
		FormDefRMSUtility formDef = new FormDefRMSUtility(FormDefRMSUtility.getUtilityName());
		formDef.addModelPrototype(new DataModelTree());
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(dataModel);
		JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().registerRMSUtility(formDef);
		
		String[] classes = {
				"org.javarosa.core.model.QuestionDef",
				"org.javarosa.core.model.GroupDef",				
		};		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
	}

}
