/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.model.condition;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.IExprDataType;
import org.javarosa.xpath.expr.XPathExpression;

/* a collection of objects that affect the evaluation of an expression, like function handlers
 * and (not supported) variable bindings
 */
public class EvaluationContext {
	private TreeReference contextNode; //unambiguous ref used as the anchor for relative paths
	private Hashtable functionHandlers;
	private Hashtable variables;
	
	public boolean isConstraint; //true if we are evaluating a constraint
	public IAnswerData candidateValue; //if isConstraint, this is the value being validated
	public boolean isCheckAddChild; //if isConstraint, true if we are checking the constraint of a parent node on how
									//  many children it may have
	
	private String outputTextForm = null; //Responsible for informing itext what form is requested if relevant
	
	private Hashtable<String, FormInstance> formInstances;
	
	FormInstance instance;
	
	/** Copy Constructor **/
	private EvaluationContext (EvaluationContext base) {
		//TODO: These should be deep, not shallow
		this.functionHandlers = base.functionHandlers;
		this.formInstances = base.formInstances;
		this.variables = base.variables;
		
		this.contextNode = base.contextNode;
		this.instance = base.instance;
		
		this.isConstraint = base.isConstraint;
		this.candidateValue = base.candidateValue;
		this.isCheckAddChild = base.isCheckAddChild;
		
		this.outputTextForm = base.outputTextForm;
	}
	
	public EvaluationContext (EvaluationContext base, TreeReference context) {
		this(base);
		this.contextNode = context;
	}
	
	public EvaluationContext (FormInstance instance, Hashtable<String, FormInstance> formInstances, EvaluationContext base) {
		this(base);
		this.formInstances = formInstances;
		this.instance = instance;
	}

	public EvaluationContext (FormInstance instance) {
		this(instance, new Hashtable<String, FormInstance>());
	}
	
	public EvaluationContext (FormInstance instance, Hashtable<String, FormInstance> formInstances) {
		this.formInstances = formInstances; 
		this.instance = instance;
		this.contextNode = TreeReference.rootRef();
		functionHandlers = new Hashtable();
		variables = new Hashtable();
	}
	
	public FormInstance getInstance(String id) {
		return formInstances.containsKey(id) ? formInstances.get(id) : null;
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
	
	public void setOutputTextForm(String form) {
		this.outputTextForm = form;
	}
	
	public String getOutputTextForm() {
		return outputTextForm;
	}
	
	public void setVariables(Hashtable<String, ?> variables) {
		for (Enumeration e = variables.keys(); e.hasMoreElements(); ) {
			String var = (String)e.nextElement();
			setVariable(var, variables.get(var));
		}
	}
	
	public void setVariable(String name, Object value) {
		//No such thing as a null xpath variable. Empty
		//values in XPath just get converted to ""
		if(value == null) {
			variables.put(name, "");
			return;
		}
		//Otherwise check whether the value is one of the normal first
		//order datatypes used in xpath evaluation
		if(value instanceof Boolean ||
				   value instanceof Double  ||
				   value instanceof String  ||
				   value instanceof Date    ||
				   value instanceof IExprDataType) {
				variables.put(name, value);
				return;
		}
		
		//Some datatypes can be trivially converted to a first order
		//xpath datatype
		if(value instanceof Integer) {
			variables.put(name, new Double(((Integer)value).doubleValue()));
			return;
		}
		if(value instanceof Float) {
			variables.put(name, new Double(((Float)value).doubleValue()));
			return;
		}
		
		//Otherwise we just hope for the best, I suppose? Should we log this?
		else {
			variables.put(name, value);
		}
	}
	
	public Object getVariable(String name) {
		return variables.get(name);
	}
	
	public Vector<TreeReference> expandReference(TreeReference ref) {
		return expandReference(ref, false);
	}

	// take in a potentially-ambiguous ref, and return a vector of refs for all nodes that match the passed-in ref
	// meaning, search out all repeated nodes that match the pattern of the passed-in ref
	// every ref in the returned vector will be unambiguous (no index will ever be INDEX_UNBOUND)
	// does not return template nodes when matching INDEX_UNBOUND, but will match templates when INDEX_TEMPLATE is explicitly set
	// return null if ref is relative, otherwise return vector of refs (but vector will be empty is no refs match)
	// '/' returns {'/'}
	// can handle sub-repetitions (e.g., {/a[1]/b[1], /a[1]/b[2], /a[2]/b[1]})
	public Vector<TreeReference> expandReference(TreeReference ref, boolean includeTemplates) {
		if (!ref.isAbsolute()) {
			return null;
		}
		
		TreeElement base = instance.getBase();
		if(ref.getInstanceName() != null && formInstances.containsKey(ref.getInstanceName())) {
			base = formInstances.get(ref.getInstanceName()).getBase();
		}

		Vector<TreeReference> v = new Vector<TreeReference>();
		expandReference(ref, base, v, includeTemplates);
		return v;
	}

	// recursive helper function for expandReference
	// sourceRef: original path we're matching against
	// node: current node that has matched the sourceRef thus far
	// templateRef: explicit path that refers to the current node
	// refs: Vector to collect matching paths; if 'node' is a target node that
	// matches sourceRef, templateRef is added to refs
	private void expandReference(TreeReference sourceRef, TreeElement node, Vector<TreeReference> refs, boolean includeTemplates) {
		int depth = node.getDepth();
		Vector<XPathExpression> predicates = null;
		if (depth == sourceRef.size()) {
			refs.addElement(node.getRef());
		} else {
			String name = sourceRef.getName(depth);
			predicates = sourceRef.getPredicate(depth);
			//ETHERTON: Is this where we should test for predicates?
			int mult = sourceRef.getMultiplicity(depth);
			Vector<TreeElement> set = new Vector<TreeElement>();
			
			if (node.getNumChildren() > 0) {
				if (mult == TreeReference.INDEX_UNBOUND) {
					int count = node.getChildMultiplicity(name);
					for (int i = 0; i < count; i++) {
						TreeElement child = node.getChild(name, i);
						if (child != null) {
							set.addElement(child);
						} else {
							throw new IllegalStateException(); // missing/non-sequential
							// nodes
						}
					}
					if (includeTemplates) {
						TreeElement template = node.getChild(name, TreeReference.INDEX_TEMPLATE);
						if (template != null) {
							set.addElement(template);
						}
					}
				} else if(mult != TreeReference.INDEX_ATTRIBUTE){
					//TODO: Make this test mult >= 0?
					//If the multiplicity is a simple integer, just get
					//the appropriate child
					TreeElement child = node.getChild(name, mult);
					if (child != null) {
						set.addElement(child);
					}
				}
			}
			
			if(mult == TreeReference.INDEX_ATTRIBUTE) {
				TreeElement attribute = node.getAttribute(null, name);
				set.addElement(attribute);
			}
	
			for (Enumeration e = set.elements(); e.hasMoreElements();) {
				//if there are predicates then we need to see if e.nextElement meets the standard of the predicate
				TreeElement treeElement = (TreeElement)e.nextElement();				
				if(predicates != null)
				{
					TreeReference treeRef = treeElement.getRef();
					boolean passedAll = true;
					for(XPathExpression xpe : predicates)
					{
						//test the predicate on the treeElement
						EvaluationContext evalContext = new EvaluationContext(this, treeRef);
						Object o = xpe.eval(instance, evalContext);
						if(o instanceof Boolean)
						{
							boolean passed = ((Boolean)o).booleanValue();
							if(!passed)
							{
								passedAll = false;
								break;
							}
						}
					}
					if(passedAll)
					{
						expandReference(sourceRef, treeElement, refs, includeTemplates);
					}
				}
				else
				{
					expandReference(sourceRef, treeElement, refs, includeTemplates);
				}
			}
		}
	}
}
