package org.javarosa.formmanager.view.summary;

import org.javarosa.core.api.State;
import org.javarosa.core.model.FormIndex;
import org.javarosa.formmanager.api.JrFormEntryController;

public class FormSummaryState implements FormSummaryTransitions, State{

	private final JrFormEntryController formController;

	public FormSummaryState(JrFormEntryController formController){
		this.formController = formController;
	}
	
	public void start() {
		FormSummaryController controller = new FormSummaryController(formController.getModel());
		controller.setTransitions(this);
		controller.start();
	}

	public void exit() {
		formController.abort();
	}

	public void saveAndExit(boolean formComplete) {
		// sending handled by CompletedFormOptionsState
		formController.saveAndExit(formComplete);
	}

	public void viewForm(FormIndex formIndex) {
		formController.start(formIndex);
	}

}
