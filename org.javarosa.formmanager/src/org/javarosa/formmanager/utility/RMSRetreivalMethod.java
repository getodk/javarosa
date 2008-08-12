package org.javarosa.formmanager.utility;

import java.io.IOException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.util.UnavailableExternalizerException;
import org.javarosa.formmanager.activity.FormEntryContext;

public class RMSRetreivalMethod implements IFormDefRetrievalMethod {

	public FormDef retreiveFormDef(Context context) {
		if(context instanceof FormEntryContext) {
			FormEntryContext formContext = (FormEntryContext)context;

			//TODO: Are we going to make this non-RMS dependant any any point?
			FormDefRMSUtility formUtil = (FormDefRMSUtility)JavaRosaServiceProvider.instance().getStorageManager().getRMSStorageProvider().getUtility(FormDefRMSUtility.getUtilityName());  //whoa!
			FormDef theForm = new FormDef();
			try {
				formUtil.retrieveFromRMS(formContext.getFormID(), theForm);
				return theForm;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (UnavailableExternalizerException uee) {
				uee.printStackTrace();
			}
		}
		return null;
	}

}
