/**
 * 
 */
package org.javarosa.formmanager.utility;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;

/**
 * @author ctsims
 *
 */
public class ModelRmsRetrievalMethod implements IFormDefRetrievalMethod {

	RMSRetreivalMethod method;
	FormInstance model;
	
	public ModelRmsRetrievalMethod(FormInstance model) {
		construct(model);
		this.model = model;
	}
	
	public ModelRmsRetrievalMethod(int modelId) {
		IStorageUtility instances = StorageManager.getStorage(FormInstance.STORAGE_KEY);
		construct((FormInstance)instances.read(modelId));
	}
	
	private void construct(FormInstance model) {
		method = new RMSRetreivalMethod(model.getFormId());
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.formmanager.utility.IFormDefRetrievalMethod#retreiveFormDef(org.javarosa.core.Context)
	 */
	public FormDef retreiveFormDef() {
		return method.retreiveFormDef();
	}
}
