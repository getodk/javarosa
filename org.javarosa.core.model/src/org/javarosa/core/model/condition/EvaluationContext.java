package org.javarosa.core.model.condition;

import java.util.Hashtable;

/* a collection of objects that affect the evaluation of an expression, like function handlers
 * and (not supported) variable bindings
 */
public class EvaluationContext {
	private Hashtable functionHandlers;
	
	public EvaluationContext () {
		functionHandlers = new Hashtable();
	}
	
	public void addFunctionHandler (IFunctionHandler fh) {
		functionHandlers.put(fh.getName(), fh);
	}
	
	public Hashtable getFunctionHandlers () {
		return functionHandlers;
	}
}
