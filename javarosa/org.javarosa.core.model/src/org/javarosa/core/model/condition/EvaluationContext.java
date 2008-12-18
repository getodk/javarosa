package org.javarosa.core.model.condition;

import java.util.Hashtable;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;

/* a collection of objects that affect the evaluation of an expression, like function handlers
 * and (not supported) variable bindings
 */
public class EvaluationContext {
	private TreeReference contextNode; //unambiguous ref used as the anchor for relative paths
	private Hashtable functionHandlers;
	
	public boolean isConstraint; //true if we are evaluating a constraint
	public IAnswerData candidateValue; //if isConstraint, this is the value being validated
	public boolean isCheckAddChild; //if isConstraint, true if we are checking the constraint of a parent node on how
									//  many children it may have
	
	public EvaluationContext (EvaluationContext base, TreeReference context) {
		this.functionHandlers = base.functionHandlers;
		this.contextNode = context;
	}
	
	public EvaluationContext () {
		functionHandlers = new Hashtable();
	}
	
	public TreeReference getContextRef () {
		return contextNode;
	}
	
	public void addFunctionHandler (IFunctionHandler fh) {
		functionHandlers.put(fh.getName(), fh);
	}
	
	public Hashtable getFunctionHandlers () {
		return functionHandlers;
	}
}
