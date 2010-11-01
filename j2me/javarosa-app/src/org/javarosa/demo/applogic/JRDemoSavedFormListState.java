package org.javarosa.demo.applogic;

import java.io.IOException;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.demo.activity.savedformlist.JRDemoSavedFormListController;
import org.javarosa.demo.activity.savedformlist.JRDemoSavedFormListTransitions;
import org.javarosa.demo.util.JRDemoUtil;

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
			send = new JRDemoFormTransportState(data) {

				public void done() {

					IStorageUtility forms = StorageManager
					.getStorage(FormInstance.STORAGE_KEY);
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
