package org.javarosa.core.model.condition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class Constraint implements Externalizable {
	public IConditionExpr constraint;
	public String constraintMsg;
	
	public Constraint () { }
	
	public Constraint (IConditionExpr constraint, String constraintMsg) {
		this.constraint = constraint;
		this.constraintMsg = constraintMsg;
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		constraint = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		constraintMsg = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(constraint));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(constraintMsg));
	}
}
