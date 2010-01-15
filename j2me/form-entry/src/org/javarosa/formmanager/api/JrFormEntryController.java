/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.formmanager.view.IFormEntryView;

/**
 * @author ctsims
 *
 */
public class JrFormEntryController extends FormEntryController {

	FormEntryTransitions transitions;
	IFormEntryView view;
	
	public JrFormEntryController(FormEntryModel model) {
		super(model);
	}

	public void setView(IFormEntryView view) {
		this.view = view;
	}
	public void setTransitions(FormEntryTransitions transitions) {
		this.transitions = transitions;
	}
	
	public void start() {
		view.show();
	}
	
	public void abort() {
		transitions.abort();
	}
	
	public void saveAndExit() {
		transitions.formEntrySaved(this.getModel().getForm(),this.getModel().getForm().getInstance(),true);
	}
	
	public void suspendActivity(int mediaType) {
		transitions.suspendForMediaCapture(mediaType);
	}
}
