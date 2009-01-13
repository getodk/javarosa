package org.javarosa.workflow.model;

import java.util.Vector;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;

/**
 * A Workflow state is a state in a Machine that is defined an overall
 * workflow. A state contains an XPath Expression which provides a test 
 * of the set of values in a Data Model that defines the state. It also
 * contains a list of possible actions which can be taken from this state,
 * and as a result, the possible edges that can be traversed from this 
 * state, to where, and with what interaction. 
 *  
 * @author Clayton Sims
 * @date Jan 13, 2009 
 *
 */
public class WorkflowState {
	
	/**
	 * The name of this state.
	 */
	String name;
	
	/**
	 * A human-readable description of this state.
	 */
	String description;
	
	/**
	 * A boolean XPathExpression which defines whether or not this is
	 * the state that the workflow should currently reside in.
	 */
	XPathExpression definition;
	
	/** Vector<IWorkflowAction> **/
	Vector actions;
	
	public WorkflowState(String name, XPathExpression definition, Vector actions) {
		this.name = name;
		this.definition = definition;
		this.actions = actions;
	}
	
	/**
	 * Determines whether or not the variables in the model reflect this as the
	 * current state of the workflow.
	 * @param model A model containing all of the values in the current workflow
	 * execution context.
	 * @return true if this state is reflected by the variables. False otherwise.
	 */
	public boolean isCurrentState(IFormDataModel model) {
		return XPathFuncExpr.toBoolean(definition.eval(model, new EvaluationContext())).booleanValue();
	}
	
	/**
	 * @return A human-readable description of this state.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the actions
	 */
	public Vector getActions() {
		return actions;
	}
	
}
