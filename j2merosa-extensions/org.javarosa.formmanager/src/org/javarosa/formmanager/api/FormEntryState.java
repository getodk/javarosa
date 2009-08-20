/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.core.api.Constants;
import org.javarosa.core.api.State;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.formmanager.controller.FormEntryController;
import org.javarosa.formmanager.controller.IControllerHost;
import org.javarosa.formmanager.model.FormEntryModel;
import org.javarosa.formmanager.utility.FormDefFetcher;
import org.javarosa.formmanager.view.IFormEntryView;

/**
 * @author ctsims
 *
 */
public class FormEntryState implements State<FormEntryTransitions>, IControllerHost {

	FormEntryModel model;
	FormEntryController controller;
	IFormEntryView view;

	FormEntryTransitions transitions;
	
	public FormEntryState(IFormEntryView view, FormDefFetcher fetcher, boolean readOnly) {
		this(view, fetcher, readOnly, FormIndex.createBeginningOfFormIndex());
	}
	public FormEntryState(IFormEntryView view, FormDefFetcher fetcher, boolean readOnly, FormIndex firstQuestion) {
		FormDef theForm = fetcher.getFormDef();
		
		//Create MVC;
		model = new FormEntryModel(theForm, theForm.getDataModel().getId(), firstQuestion); //Any reason we're not getting id inside hte method? 
		controller = new FormEntryController(model, this);
		this.view = view; // Hmmmm, created here or elsewhere?
		
		model.setReadOnly(readOnly);
	}
	

	public State<FormEntryTransitions> enter(FormEntryTransitions transitions) {
		this.transitions = transitions;
		return this;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.State#start()
	 */
	public void start() {
		controller.setView(view);
	}
	public void controllerReturn(String status) {
		if ("exit".equals(status)) {
			
			if(!model.isSaved()) {
				transitions.abort();
			} else {
				transitions.formEntrySaved(model.getForm(), model.getForm().getDataModel(), model.isFormComplete());
			}
		} else if (Constants.ACTIVITY_TYPE_GET_IMAGES.equals(status)) {
			
		}
		else if (Constants.ACTIVITY_TYPE_GET_AUDIO.equals(status)) {
			
		}
	}
	public void setView(IFormEntryView view) {
		view.show();
	}

}
