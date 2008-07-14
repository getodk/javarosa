package org.javarosa.formmanager.model;

import javax.microedition.lcdui.Displayable;

/**
 * Controller Listeners receive events from a controller regarding high
 * level requests, such as the desire to set the application's view.
 * 
 * @author Clayton Sims
 *
 */
public interface IControllerListener {
	void setView(Displayable view);
}
