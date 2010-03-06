package org.javarosa.core.model;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class ItemsetBinding implements Externalizable {
	
	public IConditionExpr nodeset;
	public IConditionExpr label;
	public boolean labelIsItext;
	public boolean copyMode; //true = copy subtree; false = copy string value
	public TreeReference copyRef;
	public IConditionExpr value;
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		nodeset = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		label = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		value = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		copyRef = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
		labelIsItext = ExtUtil.readBool(in);
		copyMode = ExtUtil.readBool(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(nodeset));
		ExtUtil.write(out, new ExtWrapTagged(label));
		ExtUtil.write(out, new ExtWrapTagged(value));
		ExtUtil.write(out, copyRef);
		ExtUtil.writeBool(out, labelIsItext);
		ExtUtil.writeBool(out, copyMode);
	}

}
