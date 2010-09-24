/**
 * 
 */
package org.javarosa.demo.applogic;

import java.io.IOException;

import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.formmanager.api.FormTransportState;

/**
 * @author ctsims
 *
 */
public abstract class JRDemoFormTransportState extends FormTransportState {
	
	public JRDemoFormTransportState(FormInstance tree, SubmissionProfile profile) throws IOException {
		super(JRDemoContext._().buildMessage(tree, profile));
	}

}
