/**
 *
 */
package org.javarosa.core.model.actions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.Action;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author ctsims
 *
 */
public class ActionWrapper extends Action {

   List<Action> listOfActions = new ArrayList<Action>(0);

	public ActionWrapper() {
		super("action");
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.Action#processAction(org.javarosa.core.model.FormDef)
	 */
	public void processAction(FormDef target, TreeReference context) {
		super.processAction(target, context);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.Action#readExternal(java.io.DataInputStream, org.javarosa.core.util.externalizable.PrototypeFactory)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		super.readExternal(in, pf);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.Action#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		super.writeExternal(out);
	}

}
