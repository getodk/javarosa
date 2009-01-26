package org.javarosa.formmanager.utility;

import java.io.IOException;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.storage.FormDefMetaData;
import org.javarosa.core.model.storage.FormDefRMSUtility;
import org.javarosa.core.services.storage.utilities.IRecordStoreEnumeration;
import org.javarosa.core.services.storage.utilities.RecordStorageException;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
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
					IRecordStoreEnumeration en = formUtil.enumerateMetaData();
					FormDefMetaData mdata;
					while(en.hasNextElement()) {
						mdata = new FormDefMetaData();
						byte[] record = en.nextRecord();
						ExtUtil.deserialize(record, mdata);
						if(mdata.getName().equals(formContext.getFormName())) {
							formUtil.retrieveFromRMS(mdata.getRecordId(), theForm);
						}
					}
					
				}
				return theForm;
			} catch (IOException e) {
				e.printStackTrace();
			} catch (DeserializationException uee) {
				uee.printStackTrace();
			} catch (RecordStorageException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
