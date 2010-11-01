/**
 * 
 */
package org.javarosa.demo.applogic;

import org.javarosa.formmanager.*;
import java.io.IOException;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.formmanager.api.FormTransportState;
import org.javarosa.model.xform.XFormSerializingVisitor;

/**
 * @author ctsims
 *
 */
public abstract class JRDemoFormTransportState extends FormTransportState {
	
	private FormInstance formTree;
	
	public JRDemoFormTransportState(FormInstance tree) throws IOException {
		super(JRDemoContext._().buildMessage(new XFormSerializingVisitor().createSerializedPayload(tree)));
		this.formTree = tree;

	}

}
