/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.core.api.State;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;

/**
 * @author ctsims
 *
 */
public abstract class FormEntryState implements FormEntryTransitions, State {

	public void start () {
		JrFormEntryController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected abstract JrFormEntryController getController ();
	
	public abstract void abort();
	
	public abstract void formEntrySaved(FormDef form, FormInstance instanceData, boolean formWasCompleted);
	
	public abstract void suspendForMediaCapture (int captureType);
}
