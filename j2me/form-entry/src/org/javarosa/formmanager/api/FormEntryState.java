/**
 * 
 */
package org.javarosa.formmanager.api;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.FormEntryTransitions;
import org.javarosa.formmanager.controller.FormEntryController;

/**
 * @author ctsims
 *
 */
public abstract class FormEntryState implements FormEntryTransitions, State {

	public void start () {
		FormEntryController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected abstract FormEntryController getController ();

}
