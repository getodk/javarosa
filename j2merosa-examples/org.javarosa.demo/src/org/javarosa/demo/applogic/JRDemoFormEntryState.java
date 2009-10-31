package org.javarosa.demo.applogic;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.demo.util.FormEntryViewFactory;
import org.javarosa.demo.util.JRDemoUtil;
import org.javarosa.formmanager.api.FormEntryState;
import org.javarosa.formmanager.controller.FormEntryController;
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
	}
	
	protected FormEntryController getController() {

		
		FormDefFetcher fetcher = new FormDefFetcher(new RMSRetreivalMethod(formName), preloaders);
		
		
		
		return new FormEntryController(new FormEntryViewFactory(), fetcher, false);
	}

	public void abort() {
		JRDemoUtil.goToList(cameFromFormList);
	}

	public void formEntrySaved(FormDef form, DataModelTree instanceData, boolean formWasCompleted) {
		if (formWasCompleted) {
			//send
			
			
		} else {
			abort();
		}
	}

	public void suspendForMediaCapture(int captureType) {
		throw new RuntimeException("not supported yet!!");
	}

}
