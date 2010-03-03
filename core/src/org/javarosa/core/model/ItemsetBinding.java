package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class ItemsetBinding implements Externalizable {
	
	public IConditionExpr nodeset;
	public TreeReference label;
	public boolean labelIsItext;
	public TreeReference dest;
	public boolean destCopy; //true = copy subtree; false = copy string value

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		nodeset = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		label = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
		dest = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
		labelIsItext = ExtUtil.readBool(in);
		destCopy = ExtUtil.readBool(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(nodeset));
		ExtUtil.write(out, label);
		ExtUtil.write(out, dest);
		ExtUtil.writeBool(out, labelIsItext);
		ExtUtil.writeBool(out, destCopy);
	}

}
