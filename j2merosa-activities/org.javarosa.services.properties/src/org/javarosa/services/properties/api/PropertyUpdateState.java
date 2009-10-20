/**
 * 
 */
package org.javarosa.services.properties.api;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.State;
import org.javarosa.j2me.view.J2MEDisplay;
import org.javarosa.services.properties.api.transitions.PropertyUpdateTransitions;
import org.javarosa.services.properties.controller.PropertyUpdateController;
import org.javarosa.services.properties.controller.PropertyUpdateControllerListener;
import org.javarosa.services.properties.view.PropertiesScreen;

/**
 * @author ctsims
 *
 */
public class PropertyUpdateState implements State<PropertyUpdateTransitions>, PropertyUpdateControllerListener {

	private PropertyUpdateTransitions transitions;
	private PropertiesScreen screen;
	
	public PropertyUpdateState() {
		screen = new PropertiesScreen(JavaRosaServiceProvider.instance().getPropertyManager());
		PropertyUpdateController controller = new PropertyUpdateController(screen);
		controller.setListener(this);
	}

	public void enter(PropertyUpdateTransitions transitions) {
		this.transitions = transitions;
	}

	public void start() {
		J2MEDisplay.setView(screen);
	}

	public void finished() {
		transitions.done();
	}
	
	

}
