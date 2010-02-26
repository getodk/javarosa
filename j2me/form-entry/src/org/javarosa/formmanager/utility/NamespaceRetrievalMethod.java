/**
 * 
 */
package org.javarosa.formmanager.utility;

import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.IStorageUtilityIndexed;
import org.javarosa.core.services.storage.StorageManager;

/**
 * Retrieves a FormDef from RMS by its XML namespace.
 * 
 * @author ctsims
 *
 */
public class NamespaceRetrievalMethod implements IFormDefRetrievalMethod {

	FormDef def;
	
	public NamespaceRetrievalMethod(String namespace) {
		IStorageUtilityIndexed forms = (IStorageUtilityIndexed)StorageManager.getStorage(FormDef.STORAGE_KEY);
		int id;
		
        Vector IDs = forms.getIDsForValue("XMLNS", namespace);
        if (IDs.size() == 1) {
        	id = ((Integer)IDs.elementAt(0)).intValue();
        } else {
        	throw new RuntimeException("No form found with namespace [" + namespace + "]");
        }

        load(id);
	}
	
	private void load(int id) {
		IStorageUtility forms = StorageManager.getStorage(FormDef.STORAGE_KEY);
		FormDef theForm = (FormDef)forms.read(id);
		
		if (theForm != null) {
			this.def = theForm;
		} else {
			String error = "Form loader couldn't retrieve form for ";
			error += " ID = " + id;
			throw new RuntimeException(error);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.formmanager.utility.IFormDefRetrievalMethod#retreiveFormDef()
	 */
	public FormDef retreiveFormDef() {
		return def;
	}
}
