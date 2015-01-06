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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
	private HashMap<String, IFunctionHandler> functionHandlers;
	private HashMap<String, Object> variables;

	public boolean isConstraint; //true if we are evaluating a constraint
	public IAnswerData candidateValue; //if isConstraint, this is the value being validated
	public boolean isCheckAddChild; //if isConstraint, true if we are checking the constraint of a parent node on how
									//  many children it may have

	private String outputTextForm = null; //Responsible for informing itext what form is requested if relevant

	private HashMap<String, FormInstance> formInstances;

	private TreeReference original;
	private int currentContextPosition = -1;

	FormInstance instance;
	int[] predicateEvaluationProgress;

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
		this.original = base.original;

		//Hrm....... not sure about this one. this only happens after a rescoping,
		//and is fixed on the context. Anything that changes the context should
		//invalidate this
		this.currentContextPosition = base.currentContextPosition;
	}

	public EvaluationContext (EvaluationContext base, TreeReference context) {
		this(base);
		this.contextNode = context;
	}

	public EvaluationContext (EvaluationContext base, HashMap<String, FormInstance> formInstances, TreeReference context) {
		this(base, context);
		this.formInstances = formInstances;
	}

	public EvaluationContext (FormInstance instance, HashMap<String, FormInstance> formInstances, EvaluationContext base) {
		this(base);
		this.formInstances = formInstances;
		this.instance = instance;
	}

	public EvaluationContext (FormInstance instance) {
		this(instance, new HashMap<String, FormInstance>());
	}

	public EvaluationContext (FormInstance instance, HashMap<String, FormInstance> formInstances) {
		this.formInstances = formInstances;
		this.instance = instance;
		this.contextNode = TreeReference.rootRef();
		functionHandlers = new HashMap<String, IFunctionHandler>();
		variables = new HashMap<String, Object>();
	}

	public FormInstance getInstance(String id) {
		return formInstances.containsKey(id) ? formInstances.get(id) :
			(instance != null && id.equals(instance.getName()) ? instance : null);
	}

	public TreeReference getContextRef () {
		return contextNode;
	}

	public void setOriginalContext(TreeReference ref) {
		this.original = ref;
	}

	public TreeReference getOriginalContext() {
		if (this.original == null) { return this.contextNode; }
		else { return this.original; }
	}

	public void addFunctionHandler (IFunctionHandler fh) {
		functionHandlers.put(fh.getName(), fh);
	}

	public HashMap<String, IFunctionHandler> getFunctionHandlers () {
		return functionHandlers;
	}

	public void setOutputTextForm(String form) {
		this.outputTextForm = form;
	}

	public String getOutputTextForm() {
		return outputTextForm;
	}

	public void setVariables(HashMap<String, ?> variables) {
    for (String var : variables.keySet()) {
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

	public List<TreeReference> expandReference(TreeReference ref) {
		return expandReference(ref, false);
	}

	// take in a potentially-ambiguous ref, and return a List of refs for all nodes that match the passed-in ref
	// meaning, search out all repeated nodes that match the pattern of the passed-in ref
	// every ref in the returned List will be unambiguous (no index will ever be INDEX_UNBOUND)
	// does not return template nodes when matching INDEX_UNBOUND, but will match templates when INDEX_TEMPLATE is explicitly set
	// return null if ref is relative, otherwise return List of refs (but list will be empty is no refs match)
	// '/' returns {'/'}
	// can handle sub-repetitions (e.g., {/a[1]/b[1], /a[1]/b[2], /a[2]/b[1]})
	public List<TreeReference> expandReference(TreeReference ref, boolean includeTemplates) {
		if (!ref.isAbsolute()) {
			return null;
		}

		FormInstance baseInstance;
		if(ref.getInstanceName() != null ) {
			baseInstance = getInstance(ref.getInstanceName());
		} else {
			baseInstance = instance;
		}

		if ( baseInstance == null ) {
			throw new RuntimeException("Unable to expand reference " + ref.toString(true) + ", no appropriate instance in evaluation context");
		}

      List<TreeReference> v = new ArrayList<TreeReference>(1);
		expandReference(ref, baseInstance, baseInstance.getRoot().getRef(), v, includeTemplates);
		return v;
	}

	// recursive helper function for expandReference
	// sourceRef: original path we're matching against
	// node: current node that has matched the sourceRef thus far
	// workingRef: explicit path that refers to the current node
	// refs: List to collect matching paths; if 'node' is a target node that
	// matches sourceRef, templateRef is added to refs
	private void expandReference(TreeReference sourceRef, FormInstance instance, TreeReference workingRef, List<TreeReference> refs, boolean includeTemplates) {
		int depth = workingRef.size();
      List<XPathExpression> predicates = null;

		//check to see if we've matched fully
		if (depth == sourceRef.size()) {
			//TODO: Do we need to clone these references?
			refs.add(workingRef);
		} else {
			//Otherwise, need to get the next set of matching references

			String name = sourceRef.getName(depth);
			predicates = sourceRef.getPredicate(depth);

			//Copy predicates for batch fetch
			if (predicates != null) {
            List<XPathExpression> predCopy = new ArrayList<XPathExpression>(predicates.size());
				for (XPathExpression xpe : predicates) {
					predCopy.add(xpe);
				}
				predicates = predCopy;
			}
			//ETHERTON: Is this where we should test for predicates?
			int mult = sourceRef.getMultiplicity(depth);
         List<TreeReference> set = new ArrayList<TreeReference>(1);

			TreeElement node = instance.resolveReference(workingRef);

			{
				if (node.getNumChildren() > 0) {
					if (mult == TreeReference.INDEX_UNBOUND) {
                  List<TreeElement> childrenWithName = node.getChildrenWithName(name);
						int count = childrenWithName.size();
						for (int i = 0; i < count; i++) {
							TreeElement child = childrenWithName.get(i);
							if ( child.getMultiplicity() != i ) {
								throw new IllegalStateException("Unexpected multiplicity mismatch");
							}
							if (child != null) {
								set.add(child.getRef());
							} else {
								throw new IllegalStateException("Missing or non-sequntial nodes expanding a reference"); // missing/non-sequential
								// nodes
							}
						}
						if (includeTemplates) {
							TreeElement template = node.getChild(name, TreeReference.INDEX_TEMPLATE);
							if (template != null) {
								set.add(template.getRef());
							}
						}
					} else if(mult != TreeReference.INDEX_ATTRIBUTE){
						//TODO: Make this test mult >= 0?
						//If the multiplicity is a simple integer, just get
						//the appropriate child
						TreeElement child = node.getChild(name, mult);
						if (child != null) {
							set.add(child.getRef());
						}
					}
				}

				if(mult == TreeReference.INDEX_ATTRIBUTE) {
					TreeElement attribute = node.getAttribute(null, name);
					if (attribute != null) {
						set.add(attribute.getRef());
					}
				}
			}

			if (predicates != null && predicateEvaluationProgress != null) {
				predicateEvaluationProgress[1] += set.size();
			}

			if (predicates != null) {
				boolean firstTime = true;
            List<TreeReference> passed = new ArrayList<TreeReference>(set.size());
				for (XPathExpression xpe : predicates) {
					for ( int i = 0 ; i < set.size() ; ++i ) {
						//if there are predicates then we need to see if e.nextElement meets the standard of the predicate
						TreeReference treeRef = set.get(i);

						//test the predicate on the treeElement
						EvaluationContext evalContext = rescope(treeRef, (firstTime ? treeRef.getMultLast() : i));
						Object o = xpe.eval(instance, evalContext);
						if(o instanceof Boolean)
						{
							boolean testOutcome = ((Boolean)o).booleanValue();
							if ( testOutcome ) {
								passed.add(treeRef);
							}
						}
					}
					firstTime = false;
					set.clear();
					set.addAll(passed);
					passed.clear();

					if(predicateEvaluationProgress != null) {
						predicateEvaluationProgress[0]++;
					}
				}
			}

			for ( int i = 0 ; i < set.size() ; ++i ) {
				TreeReference treeRef = set.get(i);
				expandReference(sourceRef, instance, treeRef, refs, includeTemplates);
			}
		}
	}

    private EvaluationContext rescope(TreeReference treeRef, int currentContextPosition) {
        EvaluationContext ec = new EvaluationContext(this, treeRef);
        // broken:
        ec.currentContextPosition = currentContextPosition;
        //If there was no original context position, we'll want to set the next original
        //context to be this rescoping (which would be the backup original one).
        if(this.original != null) {
                ec.setOriginalContext(this.getOriginalContext());
        } else {
                //Check to see if we have a context, if not, the treeRef is the original declared
                //nodeset.
                if(TreeReference.rootRef().equals(this.getContextRef()))
                {
                        ec.setOriginalContext(treeRef);
                } else {
                        //If we do have a legit context, use it!
                        ec.setOriginalContext(this.getContextRef());
                }

        }
        return ec;
    }

    public FormInstance getMainInstance() {
            return instance;
    }

    public TreeElement resolveReference(TreeReference qualifiedRef) {
            FormInstance instance = this.getMainInstance();
            if(qualifiedRef.getInstanceName() != null) {
                    instance = this.getInstance(qualifiedRef.getInstanceName());
            }
            return instance.resolveReference(qualifiedRef);
    }

    public int getContextPosition() {
            return currentContextPosition;
    }

    public void setPredicateProcessSet(int[] loadingDetails) {
            if(loadingDetails != null && loadingDetails.length == 2) {
                    predicateEvaluationProgress = loadingDetails;
            }
    }
}
