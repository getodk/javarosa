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

	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.utility.IFormDefRetrievalMethod#retreiveFormDef(org.javarosa.core.Context)
	 */
	public FormDef retreiveFormDef(Context context) {
		FormEntryContext formContext = (FormEntryContext) context;

		try {
			DataModelTreeRMSUtility modelUtil = (DataModelTreeRMSUtility) JavaRosaServiceProvider
					.instance().getStorageManager().getRMSStorageProvider()
					.getUtility(DataModelTreeRMSUtility.getUtilityName());
			IFormDataModel theModel = new DataModelTree();
			modelUtil.retrieveFromRMS(formContext.getInstanceID(), theModel);
			
			int formId = theModel.getFormId();
			
			FormDefRMSUtility formUtil = (FormDefRMSUtility) JavaRosaServiceProvider
					.instance().getStorageManager().getRMSStorageProvider()
					.getUtility(FormDefRMSUtility.getUtilityName());
			FormDef theForm = new FormDef();
			
			formUtil.retrieveFromRMS(formId, theForm);

			if(theForm == null) {
				throw new RuntimeException("Couldn't retrieve form From RMS based on model. FormId = " + formId + ". ModelId = " + formContext.getInstanceID());
			}
			
			return theForm;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("IO Exception while trying to load a Form from a saved Model ID. ModelId = " + formContext.getInstanceID());
		} catch (DeserializationException uee) {
			uee.printStackTrace();
			throw new RuntimeException("Problem deserializing while trying to load a Form from a saved Model ID. ModelId = " + formContext.getInstanceID());
		}

	}
}
