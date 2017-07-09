/**
 * 
 */
package org.javarosa.core.api;

import org.javarosa.core.util.TrivialTransitions;

/**
 * A state represents a particular state of the application. Each state has an
 * associated view and controller and a set of transitions to new states.
 * 
 * @see TrivialTransitions
 * @author ctsims
 * 
 */
public interface State {

	public void start();

}
