package org.javarosa.demo.applogic;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.condition.IFunctionHandler;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.services.storage.IStorageUtility;
import org.javarosa.core.services.storage.StorageFullException;
import org.javarosa.core.services.storage.StorageManager;
import org.javarosa.demo.util.JRDemoFormEntryViewFactory;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.formmanager.api.CompletedFormOptionsState;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.api.JrFormEntryController;
import org.javarosa.formmanager.api.JrFormEntryModel;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.utility.RMSRetreivalMethod;

public class JRDemoFormEntryState extends FormEntryState {

	protected int formID;
	protected int instanceID;

	boolean cameFromFormList;
	
	public JRDemoFormEntryState (int formID) {
		init(formID, -1, true);
	}

	public JRDemoFormEntryState (int formID, int instanceID) {
		init(formID, instanceID, false);
	}

	private void init (int formID, int instanceID, boolean cameFromFormList) {
		this.formID = formID;
		this.instanceID = instanceID;
		this.cameFromFormList = cameFromFormList;
		//In this case we did not load anything
		if (instanceID!=-1)
		{
		}
			
	}
	
	protected JrFormEntryController getController() {

		Vector<IPreloadHandler> preloaders = JRDemoContext._().getPreloaders();
		Vector<IFunctionHandler> funcHandlers = JRDemoContext._().getFuncHandlers();
		FormDefFetcher fetcher = new FormDefFetcher(new RMSRetreivalMethod(formID), preloaders, funcHandlers);
		FormDef form = fetcher.getFormDef();
		
		JrFormEntryController controller =  new JrFormEntryController(new JrFormEntryModel(form));
		String title = form.getTitle();
		if(title == null) {
			title = "Enter Data";
		}
		controller.setView(new JRDemoFormEntryViewFactory(title).getFormEntryView(controller));
		return controller;
	}

	public void abort() {
		JRDemoUtil.goToList(cameFromFormList);
	}

	public void formEntrySaved(FormDef form, FormInstance instanceData, boolean formWasCompleted) {
		System.out.println("form is complete: " + formWasCompleted);
 
		//Warning, this might be null
		final SubmissionProfile profile = form.getSubmissionProfile();
		
		if (formWasCompleted) {
			
			CompletedFormOptionsState completed = new CompletedFormOptionsState(instanceData) {

				public void sendData(FormInstance data) {
					JRDemoFormTransportState send;
					try {
						send = new JRDemoFormTransportState(data, profile) {

							public void done() {
								JRDemoUtil.goToList(cameFromFormList);
							}

							public void sendToBackground() {
								JRDemoUtil.goToList(cameFromFormList);
							}
							
						};
					} catch (IOException e) {
						throw new RuntimeException("Unable to serialize XML Payload!");
					}
					send.start();
				}

				public void sendToFreshLocation(FormInstance data) {
					throw new RuntimeException("Sending to non-default location disabled");
				}

				public void skipSend(FormInstance data) {
					IStorageUtility storage = StorageManager.getStorage(FormInstance.STORAGE_KEY);
					try {
						System.out.println("writing data: " + data.getName());
						storage.write(data);
					} catch (StorageFullException e) {
						new RuntimeException("Storage full, unable to save data.");
					}
					abort();
				}
			};
			completed.start();
		} else {
			abort();
		}
	}
}
