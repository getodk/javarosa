/**
 * 
 */
package org.javarosa.formmanager.api.transitions;


/**
 * @author ctsims
 *
 */
public interface FormBatchTransportStateTransitions {
	public void done();
	
	//TODO: Add an argument here for a thread manager
	public void sendToBackground();
}
