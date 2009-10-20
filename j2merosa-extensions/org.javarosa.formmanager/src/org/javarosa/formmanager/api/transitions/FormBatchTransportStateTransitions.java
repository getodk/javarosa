/**
 * 
 */
package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.api.Transitions;

/**
 * @author ctsims
 *
 */
public interface FormBatchTransportStateTransitions extends Transitions {
	public void done();
	
	//TODO: Add an argument here for a thread manager
	public void sendToBackground();
}
