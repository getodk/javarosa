/**
 * 
 */
package org.javarosa.services.properties.api;

import org.javarosa.core.api.State;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.services.properties.controller.PropertyUpdateController;

/**
 * @author ctsims
 *
 */
public abstract class PropertyUpdateState implements State, TrivialTransitions {

	protected PropertyUpdateController getController () {
		return new PropertyUpdateController(this);
	}
	
	public void start() {
		getController().start();
	}

}
