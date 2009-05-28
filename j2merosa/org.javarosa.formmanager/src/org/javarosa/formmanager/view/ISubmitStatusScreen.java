package org.javarosa.formmanager.view;

import org.javarosa.core.api.IView;

public interface ISubmitStatusScreen extends IView {
	/**
	 * Destroys the current status screen and cleans up any running
	 * processes.
	 */
	public void destroy();
	
	/**
	 * 
	 */
	
	public static final int ERROR = 1;
	
	/**
	 * Receive a message from the sender which breaks the contract between
	 * the sender and the screen .
	 * 
	 * @param message A coded message for an issue which arose
	 * @param details The details (if any) of the failure
	 */
	public void receiveMessage(int message, String details); 
}
