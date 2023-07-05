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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.IExprDataType;
import org.javarosa.xpath.expr.XPathExpression;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

/**
 * A collection of objects that affect the evaluation of an expression, like
 * function handlers and (not supported) variable bindings.
 */
public class EvaluationContext {

    /**
     * Unambiguous anchor reference for relative paths
     */
    private TreeReference contextNode;
    private HashMap<String, IFunctionHandler> functionHandlers;
    private IFallbackFunctionHandler fallbackFunctionHandler;
    private HashMap<String, Object> variables;

    public boolean isConstraint; //true if we are evaluating a constraint
    public IAnswerData candidateValue; //if isConstraint, this is the value being validated
    public boolean isCheckAddChild; //if isConstraint, true if we are checking the constraint of a parent node on how
    //  many children it may have

    private String outputTextForm = null; //Responsible for informing itext what form is requested if relevant

    private HashMap<String, DataInstance> formInstances;

    private TreeReference original;
    /**
     * What element in a nodeset the context is currently pointing to.
     * Used for calculating the position() xpath function.
     */
    private int currentContextPosition = -1;

    private DataInstance instance;
    private int[] predicateEvaluationProgress;

    private static final List<FilterStrategy> DEFAULT_FILTER_CHAIN = singletonList(new RawFilterStrategy());
    private List<FilterStrategy> filterStrategyChain = DEFAULT_FILTER_CHAIN;

    /**
     * Copy Constructor
     **/
    private EvaluationContext(EvaluationContext base) {
        //TODO: These should be deep, not shallow
        functionHandlers = base.functionHandlers;
        fallbackFunctionHandler = base.fallbackFunctionHandler;
        formInstances = base.formInstances;
        variables = base.variables;

        contextNode = base.contextNode;
        instance = base.instance;

        isConstraint = base.isConstraint;
        candidateValue = base.candidateValue;
        isCheckAddChild = base.isCheckAddChild;

        outputTextForm = base.outputTextForm;
        original = base.original;

        //Hrm....... not sure about this one. this only happens after a rescoping,
        //and is fixed on the context. Anything that changes the context should
        //invalidate this
        currentContextPosition = base.currentContextPosition;

        filterStrategyChain = base.filterStrategyChain;
    }

    public EvaluationContext(EvaluationContext base, List<FilterStrategy> beforeFilterStrategyChain) {
        this(base);
        this.filterStrategyChain = Stream.concat(
            beforeFilterStrategyChain.stream(),
            filterStrategyChain.stream()
        ).collect(Collectors.toList());
    }

    public EvaluationContext(EvaluationContext base, TreeReference context) {
        this(base);
        this.contextNode = context;
    }

    public EvaluationContext(EvaluationContext base, HashMap<String, DataInstance> formInstances, TreeReference context) {
        this(base, context);
        this.formInstances = formInstances;
    }

    public EvaluationContext(DataInstance instance, HashMap<String, DataInstance> formInstances, EvaluationContext base) {
        this(base);
        this.formInstances = formInstances;
        this.instance = instance;
    }

    public EvaluationContext(DataInstance instance) {
        this(instance, new HashMap<String, DataInstance>());
    }

    public EvaluationContext(DataInstance instance, HashMap<String, DataInstance> formInstances) {
        this.formInstances = formInstances;
        this.instance = instance;
        this.contextNode = TreeReference.rootRef();
        functionHandlers = new HashMap<String, IFunctionHandler>();
        variables = new HashMap<String, Object>();
    }

    public DataInstance getInstance(String id) {
        DataInstance formInstance = formInstances.get(id);
        return formInstance != null ? formInstance :
            (instance != null && id.equals(instance.getName()) ? instance : null);
    }

    public TreeReference getContextRef() {
        return contextNode;
    }

    public void setOriginalContext(TreeReference ref) {
        original = ref;
    }

    public TreeReference getOriginalContext() {
        return (original == null) ? contextNode : original;
    }

    public void addFallbackFunctionHandler(IFallbackFunctionHandler handler) {
        fallbackFunctionHandler = handler;
    }

    public void addFunctionHandler(IFunctionHandler fh) {
        functionHandlers.put(fh.getName(), fh);
    }

    public IFallbackFunctionHandler getFallbackFunctionHandler() {
        return fallbackFunctionHandler;
    }

    public HashMap<String, IFunctionHandler> getFunctionHandlers() {
        return functionHandlers;
    }

    public void setOutputTextForm(String form) {
        outputTextForm = form;
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
        if (value == null) {
            variables.put(name, "");
            return;
        }
        //Otherwise check whether the value is one of the normal first
        //order datatypes used in xpath evaluation
        if (value instanceof Boolean ||
            value instanceof Double ||
            value instanceof String ||
            value instanceof Date ||
            value instanceof IExprDataType) {
            variables.put(name, value);
            return;
        }

        //Some datatypes can be trivially converted to a first order
        //xpath datatype
        if (value instanceof Integer) {
            variables.put(name, ((Integer) value).doubleValue());
        } else if (value instanceof Float) {
            variables.put(name, ((Float) value).doubleValue());
        } else { //Otherwise we just hope for the best, I suppose? Should we log this?
            variables.put(name, value);
        }
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public List<TreeReference> expandReference(TreeReference ref) {
        return expandReference(ref, false);
    }

    /**
     * Searches for all repeated nodes that match the pattern of the 'ref'
     * argument.
     * <p>
     * '/' returns {'/'}
     * can handle sub-repetitions (e.g., {/a[1]/b[1], /a[1]/b[2], /a[2]/b[1]})
     *
     * @param ref Potentially ambiguous reference
     * @return Null if 'ref' is relative reference. Otherwise, returns a vector
     *     of references that point to nodes that match 'ref' argument. These
     *     references are unambiguous (no index will ever be INDEX_UNBOUND). Template
     *     nodes won't be included when matching INDEX_UNBOUND, but will be when
     *     INDEX_TEMPLATE is explicitly set.
     */
    public List<TreeReference> expandReference(TreeReference ref, boolean includeTemplates) {
        if (!ref.isAbsolute()) {
            return null;
        }

        final DataInstance baseInstance = (ref.getInstanceName() != null) ? getInstance(ref.getInstanceName()) : instance;

        if (baseInstance == null) {
            throw new RuntimeException("Unable to expand reference " + ref.toString(true) +
                ", no appropriate instance in evaluation context");
        }

        List<TreeReference> treeReferences = new ArrayList<>(1);
        TreeReference workingRef = baseInstance.getRoot().getRef();
        expandReferenceAccumulator(ref, baseInstance, workingRef, treeReferences, includeTemplates);
        return treeReferences;
    }

    /**
     * Recursively performs the search for all repeated nodes that match the pattern of the 'ref' argument.
     *
     * @param sourceRef      original path we're matching against
     * @param sourceInstance original node obtained from sourceRef
     * @param workingRef     explicit path that refers to the current node
     * @param refs           accumulator List to collect matching paths.
     */
    private void expandReferenceAccumulator(TreeReference sourceRef, DataInstance sourceInstance,
                                            TreeReference workingRef, List<TreeReference> refs,
                                            boolean includeTemplates) {
        final int depth = workingRef.size();

        //check to see if we've matched fully
        if (depth == sourceRef.size()) {
            //TODO: Do we need to clone these references?
            refs.add(workingRef);
            return;
        }

        // Get the next set of matching references
        final String name = sourceRef.getName(depth);
        List<XPathExpression> predicates = sourceRef.getPredicate(depth);

        // Copy predicates for batch fetch
        if (predicates != null) {
            List<XPathExpression> predCopy = new ArrayList<XPathExpression>(predicates.size());
            for (XPathExpression xpe : predicates) {
                predCopy.add(xpe);
            }
            predicates = predCopy;
        }
        //ETHERTON: Is this where we should test for predicates?
        final int mult = sourceRef.getMultiplicity(depth);
        final List<TreeReference> treeReferences = new ArrayList<>(1);

        final AbstractTreeElement node = sourceInstance.resolveReference(workingRef);

        if (node.getNumChildren() > 0) {
            if (mult == TreeReference.INDEX_UNBOUND) {
                final List<TreeElement> childrenWithName = node.getChildrenWithName(name);
                final int count = childrenWithName.size();
                for (int i = 0; i < count; i++) {
                    TreeElement child = childrenWithName.get(i);
                    if (child.getMultiplicity() != i) {
                        throw new IllegalStateException("Unexpected multiplicity mismatch");
                    }
                    treeReferences.add(child.getRef());
                }
                if (includeTemplates) {
                    AbstractTreeElement template = node.getChild(name, TreeReference.INDEX_TEMPLATE);
                    if (template != null) {
                        treeReferences.add(template.getRef());
                    }
                }
            } else if (mult != TreeReference.INDEX_ATTRIBUTE) {
                //TODO: Make this test mult >= 0?
                //If the multiplicity is a simple integer, just get
                //the appropriate child
                AbstractTreeElement child = node.getChild(name, mult);
                if (child != null) {
                    treeReferences.add(child.getRef());
                }
            }
        }

        if (mult == TreeReference.INDEX_ATTRIBUTE) {
            AbstractTreeElement attribute = node.getAttribute(null, name);
            if (attribute != null) {
                treeReferences.add(attribute.getRef());
            }
        }

        if (predicates != null && predicateEvaluationProgress != null) {
            predicateEvaluationProgress[1] += treeReferences.size();
        }

        if (predicates != null) {
            TreeReference nodeSetRef = workingRef.clone();
            nodeSetRef.add(name, -1);

            for (int i = 0; i < predicates.size(); i++) {
                List<FilterStrategy> filterChain;
                if (i == 0 && !isNested(nodeSetRef)) {
                    filterChain = filterStrategyChain;
                } else {
                    filterChain = DEFAULT_FILTER_CHAIN;
                }

                List<TreeReference> passed = filterWithPredicate(
                    sourceInstance,
                    nodeSetRef,
                    predicates.get(i),
                    treeReferences,
                    filterChain
                );

                treeReferences.clear();
                treeReferences.addAll(passed);

                if (predicateEvaluationProgress != null) {
                    predicateEvaluationProgress[0]++;
                }
            }
        }

        for (TreeReference treeRef : treeReferences) {
            expandReferenceAccumulator(sourceRef, sourceInstance, treeRef, refs, includeTemplates);
        }
    }

    private static boolean isNested(TreeReference nodeSet) {
        for (int i = 1; i < nodeSet.size(); i++) {
            if (nodeSet.getMultiplicity(i) > -1) {
                return true;
            }
        }

        return false;
    }

    @NotNull
    private List<TreeReference> filterWithPredicate(DataInstance sourceInstance, TreeReference treeReference, XPathExpression predicate, List<TreeReference> children, List<FilterStrategy> filterChain) {
        return filterWithPredicate(sourceInstance, treeReference, predicate, children, 0, filterChain);
    }

    @NotNull
    private List<TreeReference> filterWithPredicate(DataInstance sourceInstance, TreeReference treeReference, XPathExpression predicate, List<TreeReference> children, int i, List<FilterStrategy> filterChain) {
        return filterChain.get(i).filter(sourceInstance, treeReference, predicate, children, this, () -> {
            return filterWithPredicate(sourceInstance, treeReference, predicate, children, i + 1, filterChain);
        });
    }

    public EvaluationContext rescope(TreeReference treeRef, int currentContextPosition) {
        EvaluationContext ec = new EvaluationContext(this, treeRef);
        // broken:
        ec.currentContextPosition = currentContextPosition;
        //If there was no original context position, we'll want to set the next original
        //context to be this rescoping (which would be the backup original one).
        if (original != null) {
            ec.setOriginalContext(getOriginalContext());
        } else {
            //Check to see if we have a context, if not, the treeRef is the original declared
            //nodeset.
            if (TreeReference.rootRef().equals(getContextRef())) {
                ec.setOriginalContext(treeRef);
            } else {
                //If we do have a legit context, use it!
                ec.setOriginalContext(getContextRef());
            }

        }
        return ec;
    }

    public DataInstance getMainInstance() {
        return instance;
    }

    public AbstractTreeElement resolveReference(TreeReference qualifiedRef) {
        DataInstance instance = getMainInstance();
        if (qualifiedRef.getInstanceName() != null) {
            instance = getInstance(qualifiedRef.getInstanceName());
        }
        return instance.resolveReference(qualifiedRef);
    }

    public int getContextPosition() {
        return currentContextPosition;
    }

    public void setPredicateProcessSet(int[] loadingDetails) {
        if (loadingDetails != null && loadingDetails.length == 2) {
            predicateEvaluationProgress = loadingDetails;
        }
    }
}
