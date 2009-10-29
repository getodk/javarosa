/**
 * 
 */
package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.model.instance.DataModelTree;

/**
 * @author ctsims
 *
 */
public interface CompletedFormOptionsTransitions {
	public void sendData(DataModelTree data);
	public void skipSend(DataModelTree data);
	public void sendToFreshLocation(DataModelTree data);
}
