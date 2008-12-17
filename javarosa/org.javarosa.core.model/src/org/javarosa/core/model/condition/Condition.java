package org.javarosa.core.model.condition;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class Condition implements Externalizable {
	public static final int ACTION_NULL = 0;
	public static final int ACTION_SHOW = 1;
	public static final int ACTION_HIDE = 2;
	public static final int ACTION_ENABLE = 3;
	public static final int ACTION_DISABLE = 4;
	public static final int ACTION_LOCK = 5;
	public static final int ACTION_UNLOCK = 6;
	public static final int ACTION_REQUIRE = 7;
	public static final int ACTION_DONT_REQUIRE = 8;
	
	private IConditionExpr expr;
	private int trueAction;
	private int falseAction;
	private Vector targets;
	public TreeReference contextRef;  //generic ref used to turn triggers into absolute references
		
	public Condition () {
		this(null, ACTION_NULL, ACTION_NULL, null);
	}
	
	public Condition (IConditionExpr expr, int trueAction, int falseAction, TreeReference contextRef) {
		this(expr, trueAction, falseAction, contextRef, new Vector());
	}
	
	public Condition (IConditionExpr expr, int trueAction, int falseAction, TreeReference contextRef, Vector targets) {
		this.expr = expr;
		this.trueAction = trueAction;
		this.falseAction = falseAction;
		this.contextRef = contextRef;
		this.targets = targets;
	}
	
	public boolean eval (IFormDataModel model, EvaluationContext evalContext) {
		return expr.eval(model, evalContext);
	}
	
	public void apply (IFormDataModel model, EvaluationContext evalContext) {
		boolean result = eval(model, evalContext);

		for (int i = 0; i < targets.size(); i++) {
			TreeReference targetRef = ((TreeReference)targets.elementAt(i)).contextualize(evalContext.getContextRef());
			Vector v = ((DataModelTree)model).expandReference(targetRef);		
			for (int j = 0; j < v.size(); j++) {
				performAction(((DataModelTree)model).resolveReference((TreeReference)v.elementAt(j)), result ? trueAction : falseAction);
			}
		}		
	}

	private void performAction (TreeElement node, int action) {
		switch (action) {
		case ACTION_NULL: break;
		case ACTION_SHOW:         node.setRelevant(true); break;
		case ACTION_HIDE:         node.setRelevant(false); break;
		case ACTION_ENABLE:       node.setEnabled(true); break;
		case ACTION_DISABLE:      node.setEnabled(false); break;
		case ACTION_LOCK:         /* not supported */; break;
		case ACTION_UNLOCK:       /* not supported */; break;
		case ACTION_REQUIRE:      node.setRequired(true); break;
		case ACTION_DONT_REQUIRE: node.setRequired(false); break;
		}
	}
	
	public void addTarget (TreeReference target) {
		if (targets.indexOf(target) == -1)
			targets.addElement(target);
	}
	
	public Vector getTargets () {
		return targets;
	}
	
	public Vector getTriggers () {
		Vector relTriggers = expr.getTriggers();
		Vector absTriggers = new Vector();
		for (int i = 0; i < relTriggers.size(); i++) {
			absTriggers.addElement(((TreeReference)relTriggers.elementAt(i)).anchor(contextRef));
		}
		return absTriggers;		
	}
	
	//conditions are equal if they have the same actions, expression, and triggers, but NOT targets or context ref
	public boolean equals (Object o) {
		if (o instanceof Condition) {
			Condition c = (Condition)o;
			if (this == c)
				return true;
			
			if (this.trueAction == c.trueAction && this.falseAction == c.falseAction && this.expr.equals(c.expr)) {
				//check triggers
				Vector Atriggers = this.getTriggers();
				Vector Btriggers = c.getTriggers();
				
				//order and quantity don't matter; all that matters is every trigger in A exists in B and vice versa
				for (int k = 0; k < 2; k++) {
					Vector v1 = (k == 0 ? Atriggers : Btriggers);
					Vector v2 = (k == 0 ? Btriggers : Atriggers);
				
					for (int i = 0; i < v1.size(); i++) {
						if (v2.indexOf(v1.elementAt(i)) == -1) {
							return false;
						}
					}
				}
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}			
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		trueAction = ExtUtil.readInt(in);
		falseAction = ExtUtil.readInt(in);
		expr = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		contextRef = (TreeReference)ExtUtil.read(in, TreeReference.class, pf);
		targets = (Vector)ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, trueAction);
		ExtUtil.writeNumeric(out, falseAction);
		ExtUtil.write(out, new ExtWrapTagged(expr));
		ExtUtil.write(out, contextRef);
		ExtUtil.write(out, new ExtWrapList(targets));
	}	
}