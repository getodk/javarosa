/**
 * 
 */
package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.api.Transitions;
import org.javarosa.core.model.instance.DataModelTree;

/**
 * @author ctsims
 *
 */
public interface CompletedFormOptionsStateTransitions extends Transitions {
	public void sendData(DataModelTree data);
	public void skipSend();
	public void sendToFreshLocation(DataModelTree data);
}
