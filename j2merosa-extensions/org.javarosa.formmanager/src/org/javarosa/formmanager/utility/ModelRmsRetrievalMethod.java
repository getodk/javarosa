/**
 * 
 */
package org.javarosa.formmanager.utility;

import java.io.IOException;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.core.util.externalizable.DeserializationException;

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
		IStorageUtility instances = StorageManager.getStorage(DataModelTree.STORAGE_KEY);
		construct((DataModelTree)instances.read(modelId));
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
