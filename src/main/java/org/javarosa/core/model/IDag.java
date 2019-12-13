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

package org.javarosa.core.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.model.xform.XPathReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDag {
    private static final Logger logger = LoggerFactory.getLogger(IDag.class);
    private final EvalBehavior mode = EvalBehavior.Safe_2014;

    public interface EventNotifierAccessor {
        EventNotifier getEventNotifier();
    }

    protected final EventNotifierAccessor accessor;

    /**
     * NOT VALID UNTIL finalizeTriggerables() is called!!
     * <p>
     * Topologically ordered list, meaning that for any tA and tB in
     * the list, where tA comes before tB, evaluating tA cannot depend on any
     * result from evaluating tB.
     */
    protected final List<QuickTriggerable> triggerablesDAG = new ArrayList<>();

    /**
     * NOT VALID UNTIL finalizeTriggerables() is called!!
     * <p>
     * Associates repeatable nodes with the Condition that determines their
     * relevance.
     */
    protected final Map<TreeReference, QuickTriggerable> conditionRepeatTargetIndex = new HashMap<>();

    /**
     * Maps a tree reference to the set of triggerables that need to be
     * processed when the value at this reference changes.
     */
    protected final Map<TreeReference, List<QuickTriggerable>> triggerIndex = new HashMap<>();

    /**
     * List of all the triggerables in the form. Unordered.
     */
    protected final List<QuickTriggerable> unorderedTriggerables = new ArrayList<>();

    protected IDag(EventNotifierAccessor accessor) {
        this.accessor = accessor;
    }

    protected Set<QuickTriggerable> doEvaluateTriggerables(FormInstance mainInstance, EvaluationContext evalContext, Set<QuickTriggerable> tv, TreeReference anchorRef, Set<QuickTriggerable> alreadyEvaluated) {
        // tv should now contain all of the triggerable components which are going
        // to need to be addressed by this update.
        // 'triggerables' is topologically-ordered by dependencies, so evaluate
        // the triggerables in 'tv' in the order they appear in 'triggerables'
        Set<QuickTriggerable> fired = new HashSet<>();

        Map<TreeReference, List<TreeReference>> firedAnchors = new LinkedHashMap<>();

        for (QuickTriggerable qt : triggerablesDAG) {
            if (tv.contains(qt) && !alreadyEvaluated.contains(qt)) {

                List<TreeReference> affectedTriggers = qt.t.findAffectedTriggers(firedAnchors);
                if (affectedTriggers.isEmpty()) {
                    affectedTriggers.add(anchorRef);
                }

                List<EvaluationResult> evaluationResults = evaluateTriggerable(mainInstance, evalContext, qt, affectedTriggers);

                if (evaluationResults.size() > 0) {
                    fired.add(qt);

                    for (EvaluationResult evaluationResult : evaluationResults) {
                        TreeReference affectedRef = evaluationResult.getAffectedRef();

                        TreeReference key = affectedRef.genericize();
                        List<TreeReference> values = firedAnchors.get(key);
                        if (values == null) {
                            values = new ArrayList<>();
                            firedAnchors.put(key, values);
                        }
                        values.add(affectedRef);
                    }
                }

                fired.add(qt);
            }
        }

        return fired;
    }

    /**
     * Step 3 in DAG cascade. evaluate the individual triggerable expressions
     * against the anchor (the value that changed which triggered recomputation)
     */
    private List<EvaluationResult> evaluateTriggerable(FormInstance mainInstance, EvaluationContext evalContext, QuickTriggerable qt, List<TreeReference> anchorRefs) {
        List<EvaluationResult> evaluationResults = new ArrayList<>(0);

        // Contextualize the reference used by the triggerable against the anchor
        Set<TreeReference> updatedContextRef = new HashSet<>();

        for (TreeReference anchorRef : anchorRefs) {
            TreeReference contextRef = qt.t.contextualizeContextRef(anchorRef);
            if (updatedContextRef.contains(contextRef)) {
                continue;
            }

            try {

                // Now identify all of the fully qualified nodes which this
                // triggerable updates. (Multiple nodes can be updated by the same trigger)
                List<TreeReference> qualifiedList = evalContext.expandReference(contextRef);

                // Go through each one and evaluate the trigger expression
                for (TreeReference qualified : qualifiedList) {
                    EvaluationContext ec = new EvaluationContext(evalContext, qualified);
                    evaluationResults.addAll(qt.t.apply(mainInstance, ec, qualified));
                }

                boolean fired = evaluationResults.size() > 0;
                if (fired) {
                    accessor.getEventNotifier().publishEvent(new Event(qt.t.getClass().getSimpleName(), evaluationResults));
                }

                updatedContextRef.add(contextRef);
            } catch (Exception e) {
                throw new RuntimeException("Error evaluating field '" + contextRef.getNameLast() + "': " + e.getMessage(), e);
            }
        }

        return evaluationResults;
    }

    public EvalBehavior getEvalBehavior() {
        return mode;
    }

    public QuickTriggerable getTriggerableForRepeatGroup(TreeReference repeatRef) {
        return conditionRepeatTargetIndex.get(repeatRef.genericize());
    }

    private Set<QuickTriggerable> initializeTriggerables(
        FormInstance mainInstance, EvaluationContext evalContext,
        TreeReference rootRef,
        Set<QuickTriggerable> alreadyEvaluated) {
        TreeReference genericRoot = rootRef.genericize();

        Set<QuickTriggerable> applicable = new HashSet<>();
        for (int i = 0; i < triggerablesDAG.size(); i++) {
            QuickTriggerable qt = triggerablesDAG.get(i);
            for (int j = 0; j < qt.t.getTargets().size(); j++) {
                TreeReference target = qt.t.getTargets().get(j);
                if (genericRoot.isAncestorOf(target, false)) {
                    applicable.add(qt);
                    break;
                }
            }
        }

        return evaluateTriggerables(mainInstance, evalContext, applicable, rootRef, alreadyEvaluated);
    }

    /**
     * The entry point for the DAG cascade after a value is changed in the
     * model.
     *
     * @param ref The full contextualized unambiguous reference of the value
     *            that was changed.
     */
    public Collection<QuickTriggerable> triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference ref) {
        return this.triggerTriggerables(mainInstance, evalContext, ref, new HashSet<>(1));
    }

    public void deleteRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, TreeReference deleteRef, TreeElement parentElement, TreeElement deletedElement) {
        //After a repeat group has been deleted, the following repeat groups position has changed.
        //Evaluate triggerables which depend on the repeat group reference. Directly or indirectly.
        String repeatGroupName = deletedElement.getName();
        for (int i = deletedElement.getMultiplicity(); i < parentElement.getChildMultiplicity(repeatGroupName); i++) {
            TreeElement repeatGroup = parentElement.getChild(repeatGroupName, i);

            Set<QuickTriggerable> alreadyEvaluated = triggerTriggerables(mainInstance, evalContext, repeatGroup.getRef(), new HashSet<>(0));
            publishSummary("Deleted", repeatGroup.getRef(), alreadyEvaluated);

            if (repeatGroup.getRef().equals(deleteRef)) {
                // Evaluate the children triggerables only once, for the deleted repeat group.
                //  Only children of the deleted repeat group have actually changed (they're gone) and thus calculations depend
                //  on the must be re-evaluated, the following repeat groups have been shifted along with their children.
                //  If there are calculations - regardless if inside the repeat or outside - that depend on the following
                //  repeat group positions, they will fired cascade by the above code anyway.
                //  Unit test for this scenario:
                //  Safe2014DagImplTest#deleteThirdRepeatGroup_evaluatesTriggerables_indirectlyDependentOnTheRepeatGroupsNumber
                evaluateChildrenTriggerables(mainInstance, evalContext, repeatGroup, false, alreadyEvaluated);
            }
        }
    }

    public void createRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, TreeReference createRef, TreeElement parentElement, TreeElement createdElement) {
        // trigger conditions that depend on the creation of this new node
        Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0));
        publishSummary("Created (phase 1)", createRef, qtSet1);

        // initialize conditions for the node (and sub-nodes)
        Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, createRef, new HashSet<>(0));
        publishSummary("Created (phase 2)", createRef, qtSet2);

        Set<QuickTriggerable> alreadyEvaluated = new HashSet<>(qtSet1);
        alreadyEvaluated.addAll(qtSet2);

        evaluateChildrenTriggerables(mainInstance, evalContext, createdElement, true, alreadyEvaluated);
    }

    private void evaluateChildrenTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeElement newNode, boolean createdOrDeleted, Set<QuickTriggerable> alreadyEvaluated) {
        // iterate into the group children and evaluate any triggerables that
        // depend one them, if they are not already calculated.
        int numChildren = newNode.getNumChildren();
        for (int i = 0; i < numChildren; i++) {
            TreeReference anchorRef = newNode.getChildAt(i).getRef();
            Set<QuickTriggerable> childTriggerables = triggerTriggerables(mainInstance, evalContext, anchorRef, alreadyEvaluated);
            publishSummary((createdOrDeleted ? "Created" : "Deleted"), anchorRef, childTriggerables);
        }
    }

    public void copyItemsetAnswer(FormInstance mainInstance, EvaluationContext evalContext, TreeReference copyRef, TreeElement copyToElement) {
        TreeReference targetRef = copyToElement.getRef();

        // trigger conditions that depend on the creation of these new nodes
        Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, copyRef, new HashSet<>(0));

        publishSummary("Copied itemset answer (phase 1)", targetRef, qtSet1);

        // initialize conditions for the node (and sub-nodes)
        Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, copyRef, new HashSet<>(0));
        publishSummary("Copied itemset answer (phase 2)", targetRef, qtSet2);
        // not 100% sure this will work since destRef is ambiguous as the last
        // step, but i think it's supposed to work
    }

    private QuickTriggerable findTriggerable(Triggerable t) {
        for (QuickTriggerable qt : unorderedTriggerables) {
            if (t.equals(qt.t)) {
                return qt;
            }
        }
        return null;
    }

    public final Triggerable addTriggerable(Triggerable t) {
        QuickTriggerable qt = findTriggerable(t);
        if (qt != null) {
            // one node may control access to many nodes; this means many nodes
            // effectively have the same condition
            // let's identify when conditions are the same, and store and calculate
            // it only once

            // nov-2-2011: ctsims - We need to merge the context nodes together
            // whenever we do this (finding the highest
            // common ground between the two), otherwise we can end up failing to
            // trigger when the ignored context
            // exists and the used one doesn't

            Triggerable existingTriggerable = qt.t;

            existingTriggerable.changeContextRefToIntersectWithTriggerable(t);

            return existingTriggerable;

            // note, if the contextRef is unnecessarily deep, the condition will be
            // evaluated more times than needed
            // perhaps detect when 'identical' condition has a shorter contextRef,
            // and use that one instead?

        } else {
            qt = new QuickTriggerable(t);
            unorderedTriggerables.add(qt);

            Set<TreeReference> triggers = t.getTriggers();
            for (TreeReference trigger : triggers) {
                List<QuickTriggerable> triggered = triggerIndex.get(trigger);
                if (triggered == null) {
                    triggered = new ArrayList<>();
                    triggerIndex.put(trigger.clone(), triggered);
                }
                if (!triggered.contains(qt)) {
                    triggered.add(qt);
                }
            }

            return t;
        }
    }

    /**
     * Finalize the DAG associated with the form's triggered conditions. This
     * will create the appropriate ordering and dependencies to ensure the
     * conditions will be evaluated in the appropriate orders.
     *
     * @throws IllegalStateException - If the trigger ordering contains an illegal cycle and the
     *                               triggers can't be laid out appropriately
     */
    public void finalizeTriggerables(FormInstance mainInstance, EvaluationContext evalContext) throws IllegalStateException {
        //
        // DAGify the triggerables based on dependencies and sort them so that
        // triggerables come only after the triggerables they depend on
        //
        List<QuickTriggerable> vertices = new ArrayList<>(unorderedTriggerables);
        triggerablesDAG.clear();
        List<QuickTriggerable[]> partialOrdering = new ArrayList<>();
        Set<QuickTriggerable> newDestinationSet = new HashSet<>();
        for (QuickTriggerable qt : vertices) {
            Set<QuickTriggerable> deps = new HashSet<>();
            newDestinationSet.clear();
            fillTriggeredElements(mainInstance, evalContext, qt, deps, newDestinationSet);

            // remove any self-reference if we have one...
            deps.remove(qt);
            for (QuickTriggerable qu : deps) {
                QuickTriggerable[] edge = {qt, qu};
                partialOrdering.add(edge);
            }

            // save for aggressive 2014 behavior
            qt.t.setImmediateCascades(deps);
        }

        List<QuickTriggerable> orderedRoots = new ArrayList<>();
        Set<QuickTriggerable> roots = new HashSet<>(
            vertices.size());
        int waveCount = -1;
        while (vertices.size() > 0) {
            ++waveCount;
            // determine root nodes
            roots.clear();
            roots.addAll(vertices);
            for (int i = 0; i < partialOrdering.size(); i++) {
                QuickTriggerable[] edge = partialOrdering.get(i);
                roots.remove(edge[1]);
            }

            // if no root nodes while graph still has nodes, graph has cycles
            if (roots.size() == 0) {
                String hints = "";
                for (QuickTriggerable qt : vertices) {
                    for (TreeReference r : qt.t.getTargets()) {
                        hints += "\n" + r.toString(true);
                    }
                }
                String message = "Cycle detected in form's relevant and calculation logic!";
                if (!hints.equals("")) {
                    message += "\nThe following nodes are likely involved in the loop:"
                        + hints;
                }
                throw new IllegalStateException(message);
            }

            // order the root nodes - so the order is fixed
            orderedRoots.clear();
            orderedRoots.addAll(roots);
            Collections.sort(orderedRoots, QuickTriggerable.quickTriggerablesRootOrdering);

            // remove root nodes and edges originating from them
            // add them to the triggerablesDAG.
            for (int i = 0; i < orderedRoots.size(); i++) {
                QuickTriggerable root = orderedRoots.get(i);
                root.t.setWaveCount(waveCount);
                triggerablesDAG.add(root);
                vertices.remove(root);
            }
            for (int i = partialOrdering.size() - 1; i >= 0; i--) {
                QuickTriggerable[] edge = partialOrdering.get(i);
                if (roots.contains(edge[0]))
                    partialOrdering.remove(i);
            }
        }

        //
        // build the condition index for repeatable nodes
        //

        conditionRepeatTargetIndex.clear();
        for (QuickTriggerable qt : triggerablesDAG) {
            if (qt.t instanceof Condition) {
                List<TreeReference> targets = qt.t.getTargets();
                for (TreeReference target : targets) {
                    if (mainInstance.getTemplate(target) != null) {
                        conditionRepeatTargetIndex.put(target, qt);
                    }
                }
            }
        }
    }

    /**
     * Get all of the elements which will need to be evaluated (in order) when
     * the triggerable is fired.
     */
    public void fillTriggeredElements(FormInstance mainInstance, EvaluationContext evalContext, QuickTriggerable qt, Set<QuickTriggerable> destinationSet, Set<QuickTriggerable> newDestinationSet) {
        if (qt.t.canCascade()) {
            boolean expandRepeatables = true;

            for (int j = 0; j < qt.t.getTargets().size(); j++) {
                TreeReference target = qt.t.getTargets().get(j);
                Set<TreeReference> updatedNodes = new HashSet<>();
                updatedNodes.add(target);

                // For certain types of triggerables, the update will affect
                // not only the target, but also the children of the target.
                // In that case, we want to add all of those nodes
                // to the list of updated elements as well.
                if (qt.t.isCascadingToChildren()) {
                    addChildrenOfReference(mainInstance, evalContext,
                        target, updatedNodes, expandRepeatables);
                }

                // Now go through each of these updated nodes (generally
                // just 1 for a normal calculation,
                // multiple nodes if there's a relevance cascade.
                for (TreeReference ref : updatedNodes) {
                    // Check our index to see if that target is a Trigger
                    // for other conditions
                    // IE: if they are an element of a different calculation
                    // or relevancy calc

                    // We can't make this reference generic before now or
                    // we'll lose the target information,
                    // so we'll be more inclusive than needed and see if any
                    // of our triggers are keyed on the predicate-less path
                    // of this ref
                    List<QuickTriggerable> triggered = triggerIndex.get(ref.hasPredicates() ? ref.removePredicates() : ref);

                    if (triggered != null) {
                        // If so, walk all of these triggerables that we
                        // found
                        for (QuickTriggerable qu : triggered) {
                            // And add them to the queue if they aren't
                            // there already
                            if (!destinationSet.contains(qu)) {
                                destinationSet.add(qu);
                                newDestinationSet.add(qu);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Step 2 in evaluating DAG computation updates from a value being changed
     * in the instance. This step is responsible for taking the root set of
     * directly triggered conditions, identifying which conditions should
     * further be triggered due to their update, and then dispatching all of the
     * evaluations.
     */
    private Set<QuickTriggerable> evaluateTriggerables(FormInstance mainInstance, EvaluationContext evalContext, Set<QuickTriggerable> tv, TreeReference anchorRef, Set<QuickTriggerable> alreadyEvaluated) {
        // add all cascaded triggerables to queue

        // Iterate through all of the currently known triggerables to be
        // triggered
        Set<QuickTriggerable> refSet = new HashSet<>(tv);
        for (; !refSet.isEmpty(); ) {
            Set<QuickTriggerable> newSet = new HashSet<>();
            for (QuickTriggerable qt : refSet) {
                // leverage the saved DAG edges.
                // This may over-fill the set of triggerables.
                // but should be faster than recomputing the edges.
                // with value-change optimizations, this should be
                // much faster.
                for (QuickTriggerable qu : qt.t.getImmediateCascades()) {
                    if (!tv.contains(qu)) {
                        tv.add(qu);
                        newSet.add(qu);
                    }
                }
            }
            refSet = newSet;
        }

        return doEvaluateTriggerables(mainInstance, evalContext, tv, anchorRef, alreadyEvaluated);
    }

    /**
     * This is a utility method to get all of the references of a node. It can
     * be replaced when we support dependent XPath Steps (IE: /path/to//)
     */
    private void addChildrenOfReference(FormInstance mainInstance, EvaluationContext evalContext, TreeReference original, Set<TreeReference> toAdd, boolean expandRepeatables) {
        // original has already been added to the 'toAdd' list.

        TreeElement repeatTemplate = expandRepeatables ? mainInstance.getTemplatePath(original) : null;
        if (repeatTemplate != null) {
            for (int i = 0; i < repeatTemplate.getNumChildren(); ++i) {
                TreeElement child = repeatTemplate.getChildAt(i);
                toAdd.add(child.getRef().genericize());
                addChildrenOfElement(mainInstance, evalContext, child, toAdd, expandRepeatables);
            }
        } else {
            List<TreeReference> refSet = evalContext.expandReference(original);
            for (TreeReference ref : refSet) {
                addChildrenOfElement(mainInstance, evalContext, evalContext.resolveReference(ref), toAdd, expandRepeatables);
            }
        }
    }

    // Recursive step of utility method
    private void addChildrenOfElement(FormInstance mainInstance, EvaluationContext evalContext, AbstractTreeElement<?> el, Set<TreeReference> toAdd, boolean expandRepeatables) {
        TreeElement repeatTemplate = expandRepeatables ? mainInstance.getTemplatePath(el.getRef()) : null;
        if (repeatTemplate != null) {
            for (int i = 0; i < repeatTemplate.getNumChildren(); ++i) {
                TreeElement child = repeatTemplate.getChildAt(i);
                toAdd.add(child.getRef().genericize());
                addChildrenOfElement(mainInstance, evalContext, child, toAdd, expandRepeatables);
            }
        } else {
            for (int i = 0; i < el.getNumChildren(); ++i) {
                AbstractTreeElement<?> child = el.getChildAt(i);
                toAdd.add(child.getRef().genericize());
                addChildrenOfElement(mainInstance, evalContext, child, toAdd, expandRepeatables);
            }
        }
    }

    /**
     * Walks the current set of conditions, and evaluates each of them with the
     * current context.
     */
    public Collection<QuickTriggerable> initializeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference rootRef) {
        return initializeTriggerables(mainInstance, evalContext, rootRef, new HashSet<>(1));
    }

    /**
     * Invoked to validate a filled-in form. Sweeps through from beginning
     * to end, confirming that the entered values satisfy all constraints.
     * The FormEntryController is based upon the FormDef, but has its own
     * model and controller independent of anything at the UI layer.
     */
    public ValidateOutcome validate(FormEntryController formEntryControllerToBeValidated, boolean markCompleted) {

        formEntryControllerToBeValidated.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        int event;
        while ((event =
            formEntryControllerToBeValidated.stepToNextEvent()) != FormEntryController.EVENT_END_OF_FORM) {
            if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                FormIndex formControllerToBeValidatedFormIndex = formEntryControllerToBeValidated.getModel().getFormIndex();

                int saveStatus = formEntryControllerToBeValidated.answerQuestion(
                    formControllerToBeValidatedFormIndex,
                    formEntryControllerToBeValidated.getModel().getQuestionPrompt().getAnswerValue(),
                    false
                );
                if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) {
                    // jump to the error
                    return new ValidateOutcome(formControllerToBeValidatedFormIndex, saveStatus);
                }
            }
        }
        return null;
    }

    private Set<QuickTriggerable> triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference ref, Set<QuickTriggerable> alreadyEvaluated) {

        // turn unambiguous ref into a generic ref
        // to identify what nodes should be triggered by this
        // reference changing
        TreeReference genericRef = ref.genericize();

        // get triggerables which are activated by the generic reference
        List<QuickTriggerable> triggered = triggerIndex.get(genericRef);
        if (triggered == null) {
            return alreadyEvaluated;
        }

        Set<QuickTriggerable> triggeredCopy = new HashSet<>(triggered);

        // Evaluate all of the triggerables in our new set
        return evaluateTriggerables(mainInstance, evalContext, triggeredCopy, ref, alreadyEvaluated);
    }

    public boolean shouldTrustPreviouslyCommittedAnswer() {
        return false;
    }

    protected void publishSummary(String lead, Collection<QuickTriggerable> quickTriggerables) {
        publishSummary(lead, null, quickTriggerables);
    }

    protected final void publishSummary(String lead, TreeReference ref, Collection<QuickTriggerable> quickTriggerables) {
        accessor.getEventNotifier().publishEvent(new Event(lead + ": " + (ref != null ? ref.toShortString() + ": " : "") + quickTriggerables.size() + " triggerables were fired."));
    }

    /**
     * For debugging
     */
    public final void printTriggerables(String path) {
        OutputStreamWriter w = null;
        try {
            w = new OutputStreamWriter(new FileOutputStream(new File(
                path)), "UTF-8");
            for (int i = 0; i < triggerablesDAG.size(); i++) {
                QuickTriggerable qt = triggerablesDAG.get(i);
                w.write(Integer.toString(i) + ": ");
                qt.t.print(w);
            }
        } catch (UnsupportedEncodingException e) {
            logger.error("Error", e);
        } catch (FileNotFoundException e) {
            logger.error("Error", e);
        } catch (IOException e) {
            logger.error("Error", e);
        } finally {
            if (w != null) {
                try {
                    w.flush();
                    w.close();
                } catch (IOException e) {
                    logger.error("Error", e);
                }
            }
        }
    }

    /**
     * Pull this in from FormOverview so that we can make fields private.
     */
    public final IConditionExpr getConditionExpressionForTrueAction(FormInstance mainInstance, TreeElement instanceNode, int action) {
        IConditionExpr expr = null;
        for (int i = 0; i < triggerablesDAG.size() && expr == null; i++) {
            // Clayton Sims - Jun 1, 2009 : Not sure how legitimate this
            // cast is. It might work now, but break later.
            // Clayton Sims - Jun 24, 2009 : Yeah, that change broke things.
            // For now, we won't bother to print out anything that isn't
            // a condition.
            QuickTriggerable qt = triggerablesDAG.get(i);
            if (qt.t instanceof Condition) {
                Condition c = (Condition) qt.t;

                if (c.trueAction == action) {
                    List<TreeReference> targets = c.getTargets();
                    for (int j = 0; j < targets.size() && expr == null; j++) {
                        TreeReference target = targets.get(j);

                        TreeReference tr = (TreeReference) (new XPathReference(target)).getReference();
                        TreeElement element = mainInstance.getTemplatePath(tr);
                        if (instanceNode == element) {
                            expr = c.getExpr();
                        }
                    }
                }
            }
        }
        return expr;
    }

    /**
     * API for retrieving the list of conditions, for use when
     * serializing the form definition (e.g., into .cache file).
     */
    public List<Condition> getConditions() {
        List<Condition> conditions = new ArrayList<>();
        for (QuickTriggerable qt : unorderedTriggerables) {
            if (qt.t instanceof Condition) {
                conditions.add((Condition) qt.t);
            }
        }
        return conditions;
    }

    /**
     * API for retrieving thelist of recalculates, for use when
     * serializing the form definition (e.g., into .cache file).
     */
    public final ArrayList<Recalculate> getRecalculates() {
        ArrayList<Recalculate> recalculates = new ArrayList<>();
        for (QuickTriggerable qt : unorderedTriggerables) {
            if (qt.t instanceof Recalculate) {
                recalculates.add((Recalculate) qt.t);
            }
        }
        return recalculates;
    }

    public void reportDependencyCycles() {
        Set<TreeReference> vertices = new HashSet<>();
        List<TreeReference[]> edges = new ArrayList<>();

        //build graph
        List<TreeReference> targets = new ArrayList<>();
        for (TreeReference trigger : triggerIndex.keySet()) {
            vertices.add(trigger);
            List<QuickTriggerable> triggered = triggerIndex.get(trigger);
            targets.clear();
            for (QuickTriggerable qt : triggered) {
                Triggerable t = qt.t;
                for (int j = 0; j < t.getTargets().size(); j++) {
                    TreeReference target = t.getTargets().get(j);
                    if (!targets.contains(target))
                        targets.add(target);
                }
            }

            for (int i = 0; i < targets.size(); i++) {
                TreeReference target = targets.get(i);
                vertices.add(target);

                TreeReference[] edge = {trigger, target};
                edges.add(edge);
            }
        }

        //find cycles
        boolean acyclic = true;
        Set<TreeReference> leaves = new HashSet<>(vertices.size());
        while (vertices.size() > 0) {
            //determine leaf nodes
            leaves.clear();
            leaves.addAll(vertices);
            for (int i = 0; i < edges.size(); i++) {
                TreeReference[] edge = edges.get(i);
                leaves.remove(edge[0]);
            }

            //if no leaf nodes while graph still has nodes, graph has cycles
            if (leaves.size() == 0) {
                acyclic = false;
                break;
            }

            //remove leaf nodes and edges pointing to them
            for (TreeReference leaf : leaves) {
                vertices.remove(leaf);
            }
            for (int i = edges.size() - 1; i >= 0; i--) {
                TreeReference[] edge = edges.get(i);
                if (leaves.contains(edge[1]))
                    edges.remove(i);
            }
        }

        if (!acyclic) {
            StringBuilder b = new StringBuilder();
            b.append("XPath Dependency Cycle:\n");
            for (int i = 0; i < edges.size(); i++) {
                TreeReference[] edge = edges.get(i);
                b.append(edge[0].toString()).append(" => ").append(edge[1].toString()).append("\n");
            }
            logger.error("XForm Parse Error: {}", b.toString());

            throw new RuntimeException("Dependency cycles amongst the xpath expressions in relevant/calculate");
        }
    }

}
