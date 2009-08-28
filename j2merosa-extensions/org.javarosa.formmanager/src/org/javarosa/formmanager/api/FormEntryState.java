/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Displayable;

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
import org.javarosa.formmanager.view.IFormEntryViewFactory;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class FormEntryState implements State<FormEntryTransitions>, IControllerHost {

	FormEntryModel model;
	FormEntryController controller;
	IFormEntryView view;

	FormEntryTransitions transitions;
	
	public FormEntryState(IFormEntryViewFactory view, FormDefFetcher fetcher, boolean readOnly) {
		this(view, fetcher, readOnly, null);
	}
	
	public FormEntryState(IFormEntryViewFactory view, FormDefFetcher fetcher, boolean readOnly, FormIndex firstQuestion) {
		FormDef theForm = fetcher.getFormDef();
		
		//Create MVC;
		model = new FormEntryModel(theForm, theForm.getDataModel().getId(), firstQuestion); //Any reason we're not getting id inside hte method? 
		controller = new FormEntryController(model, this);
		this.view = view.getFormEntryView("Title", model, controller); // Hmmmm, created here or elsewhere?
		
		model.setReadOnly(readOnly);
	}
	

	public void enter(FormEntryTransitions transitions) {
		this.transitions = transitions;
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.api.State#start()
	 */
	public void start() {
		view.show();
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
	public void setView(Displayable view) {
		J2MEDisplay.setView(view);
	}
}
