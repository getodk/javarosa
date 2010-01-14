/**
 * 
 */
package org.javarosa.formmanager.api;

import javax.microedition.lcdui.Displayable;

import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.j2me.view.J2MEDisplay;

/**
 * @author ctsims
 *
 */
public class JrFormEntryController extends FormEntryController {

	FormEntryTransitions transitions;
	Displayable view;
	
	public JrFormEntryController(FormEntryModel model) {
		super(model);
	}

	public void setView(Displayable view) {
		this.view = view;
	}
	public void setTransitions(FormEntryTransitions transitions) {
		this.transitions = transitions;
	}
	
	public void start() {
		J2MEDisplay.setView(view);
	}
	
	public void abort() {
		transitions.abort();
	}
	
	public void saveAndExit() {
		transitions.formEntrySaved(this.getModel().getForm(),this.getModel().getForm().getDataModel(),true);
	}
	
	public void suspendActivity(int mediaType) {
		transitions.suspendForMediaCapture(mediaType);
	}
}
