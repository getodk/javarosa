package org.javarosa.demo.applogic;

import java.io.IOException;

import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.demo.activity.savedformlist.JRDemoSavedFormListController;
import org.javarosa.demo.activity.savedformlist.JRDemoSavedFormListTransitions;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.ModelRmsRetrievalMethod;

public class JRDemoSavedFormListState implements JRDemoSavedFormListTransitions{
	public void start() {
		JRDemoSavedFormListController ctrl = new JRDemoSavedFormListController();
		ctrl.setTransitions(this);
		ctrl.start();
	}

	public void back() {
		new JRDemoFormListState().start();
	}

	public void savedFormSelected(int formID,int instanceID) {
	}
	
	public void sendDataFormInstance(final FormInstance data) {
		JRDemoFormTransportState send;
		try {
			//Link to the FormDef so we can find the appropriate submission profile
			FormDefFetcher fd = new FormDefFetcher(new ModelRmsRetrievalMethod(data), JRDemoContext._().getPreloaders(), JRDemoContext._().getFuncHandlers());
			SubmissionProfile profile = fd.getFormDef().getSubmissionProfile();
			send = new JRDemoFormTransportState(data, profile) {

				public void done() {

					IStorageUtility forms = StorageManager.getStorage(FormInstance.STORAGE_KEY);
					forms.remove(data);
					new JRDemoSavedFormListState().start();				
				}

				public void sendToBackground() {
					new JRDemoSavedFormListState().start();
				}
			};
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize XML Payload!");
		}
		send.start();
	}

}
