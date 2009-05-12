package org.javarosa.formmanager.utility;

import java.io.IOException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.formmanager.activity.FormEntryContext;

public class RMSRetreivalMethod implements IFormDefRetrievalMethod {

	public FormDef retreiveFormDef(Context context) {
		if(context instanceof FormEntryContext) {
			FormEntryContext formContext = (FormEntryContext)context;

			//TODO: Are we going to make this non-RMS dependant any any point?
			FormDefRMSUtility formUtil = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
			FormDef theForm = new FormDef();
			try {
				if(formContext.getFormID() != -1) {
					formUtil.retrieveFromRMS(formContext.getFormID(), theForm);
				} else if(formContext.getFormName() != null) {
					formUtil.retrieveFromRMS(formUtil.getIDfromName(formContext.getFormName()), theForm);
				}
				//TODO: Better heuristic for whether retrieval worked!
				if(theForm.getID() != -1) {
					return theForm;
				} else {
					String error = "Form loader couldn't retrieve form for ";
					if(formContext.getFormID() != -1) {
						error += " ID = " + formContext.getFormID();
					} else if(formContext.getFormName() != null) {
						error += " Name = " + formContext.getFormName();
					}
					throw new RuntimeException(error);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DeserializationException uee) {
				uee.printStackTrace();
			} 
		}
		return null;
	}

}
