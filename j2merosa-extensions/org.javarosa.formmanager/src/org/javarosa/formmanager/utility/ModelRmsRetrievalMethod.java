/**
 * 
 */
package org.javarosa.formmanager.utility;

import java.io.IOException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.storage.DataModelTreeRMSUtility;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.formmanager.activity.FormEntryContext;

/**
 * @author ctsims
 *
 */
public class ModelRmsRetrievalMethod implements IFormDefRetrievalMethod {

	RMSRetreivalMethod method;
	
	public ModelRmsRetrievalMethod(DataModelTree model) throws IOException, DeserializationException  {
		construct(model);
	}
	
	public ModelRmsRetrievalMethod(int modelId) throws DeserializationException, IOException {
		DataModelTreeRMSUtility modelUtil = (DataModelTreeRMSUtility) JavaRosaServiceProvider
			.instance().getStorageManager().getRMSStorageProvider()
			.getUtility(DataModelTreeRMSUtility.getUtilityName());
		
			DataModelTree theModel = new DataModelTree();
			
			modelUtil.retrieveFromRMS(modelId, theModel);
			
			construct(theModel);
	}
	
	private void construct(DataModelTree model) throws IOException, DeserializationException  {
		method = new RMSRetreivalMethod(model.getFormId());
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.utility.IFormDefRetrievalMethod#retreiveFormDef(org.javarosa.core.Context)
	 */
	public FormDef retreiveFormDef() {
		return method.retreiveFormDef();
	}
}
