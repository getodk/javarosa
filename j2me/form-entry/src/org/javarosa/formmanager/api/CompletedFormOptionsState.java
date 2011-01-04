package org.javarosa.formmanager.api;

import org.javarosa.core.api.State;
import org.javarosa.formmanager.api.transitions.CompletedFormOptionsTransitions;
import org.javarosa.services.transport.TransportMessage;

public abstract class CompletedFormOptionsState implements CompletedFormOptionsTransitions, State {

	private TransportMessage message;
	
	public CompletedFormOptionsState (TransportMessage message) {
		this.message = message;
	}
	
	public void start () {
		CompletedFormOptionsController controller = getController();
		controller.setTransitions(this);
		controller.start();
	}
	
	protected CompletedFormOptionsController getController () {
		return new CompletedFormOptionsController(message);
	}
	
}
