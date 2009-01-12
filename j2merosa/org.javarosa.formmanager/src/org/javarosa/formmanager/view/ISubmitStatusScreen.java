package org.javarosa.formmanager.view;

import org.javarosa.core.api.IView;

public interface ISubmitStatusScreen extends IView {
	/**
	 * Destroys the current status screen and cleans up any running
	 * processes.
	 */
	public void destroy();
}
