/**
 * 
 */
package org.javarosa.services.properties.api;

import org.javarosa.core.api.State;
import org.javarosa.core.services.PropertyManager;
import org.javarosa.core.util.TrivialTransitions;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.properties.controller.PropertyUpdateController;
import org.javarosa.services.properties.controller.PropertyUpdateControllerListener;
import org.javarosa.services.properties.view.PropertiesScreen;

/**
 * @author ctsims
 *
 */
public abstract class PropertyUpdateState implements State, TrivialTransitions {

	private PropertiesScreen screen;
	
	public PropertyUpdateState() {
		screen = new PropertiesScreen(PropertyManager._());
		PropertyUpdateController controller = new PropertyUpdateController(this, screen);
	}

	public void start() {
		J2MEDisplay.setView(screen);
	}

}
