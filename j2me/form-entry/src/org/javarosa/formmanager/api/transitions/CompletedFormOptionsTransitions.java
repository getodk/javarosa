/**
 * 
 */
package org.javarosa.formmanager.api.transitions;

import org.javarosa.core.model.instance.FormInstance;

/**
 * @author ctsims
 *
 */
public interface CompletedFormOptionsTransitions {
	public void sendData(FormInstance data);
	public void skipSend(FormInstance data);
	public void sendToFreshLocation(FormInstance data);
}
