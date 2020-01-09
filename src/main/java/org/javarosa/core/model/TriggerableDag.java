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

import static java.util.Collections.emptySet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
import org.javarosa.form.api.FormEntryController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TriggerableDag {
    private static final Logger logger = LoggerFactory.getLogger(TriggerableDag.class);

    public interface EventNotifierAccessor {
        EventNotifier getEventNotifier();
    }

    protected final EventNotifierAccessor accessor;

    protected final Set<QuickTriggerable> allTriggerables = new HashSet<>();
    protected Set<QuickTriggerable> triggerablesDAG = emptySet();

    protected Map<TreeReference, QuickTriggerable> repeatConditionsPerTargets = new HashMap<>();
    protected final Map<TreeReference, Set<QuickTriggerable>> triggerablesPerTrigger = new HashMap<>();


    protected TriggerableDag(EventNotifierAccessor accessor) {
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

                List<TreeReference> affectedTriggers = qt.findAffectedTriggers(firedAnchors);
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
            TreeReference contextRef = qt.contextualizeContextRef(anchorRef);
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
                    evaluationResults.addAll(qt.apply(mainInstance, ec, qualified));
                }

                boolean fired = evaluationResults.size() > 0;
                if (fired) {
                    accessor.getEventNotifier().publishEvent(new Event(qt.isCondition() ? "Condition" : "Recalculate", evaluationResults));
                }

                updatedContextRef.add(contextRef);
            } catch (Exception e) {
                throw new RuntimeException("Error evaluating field '" + contextRef.getNameLast() + "': " + e.getMessage(), e);
            }
        }

        return evaluationResults;
    }

    public QuickTriggerable getTriggerableForRepeatGroup(TreeReference repeatRef) {
        return repeatConditionsPerTargets.get(repeatRef.genericize());
    }

    private Set<QuickTriggerable> initializeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference rootRef, Set<QuickTriggerable> alreadyEvaluated) {
        TreeReference genericRoot = rootRef.genericize();

        Set<QuickTriggerable> applicable = new HashSet<>();
        for (QuickTriggerable qt : triggerablesDAG) {
            for (TreeReference target : qt.getTargets()) {
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

    public void createRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, TreeReference createRef, TreeElement createdElement) {
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
        for (QuickTriggerable qt : allTriggerables) {
            if (qt.contains(t)) {
                return qt;
            }
        }
        return null;
    }

    /**
     * Adds the provided triggerable to the DAG.
     * <p>
     * This method has side-effects:
     * <ul>
     *     <li>If a similar triggerable has been already added, its context gets intersected with the provided triggerable to cover both using only one entry</li>
     *     <li>This method builds the index of triggerables per trigger ref</li>
     * </ul>
     */
    Triggerable addTriggerable(Triggerable triggerable) {
        QuickTriggerable existingQuickTriggerable = findTriggerable(triggerable);
        if (existingQuickTriggerable != null) {
            // We found a quick triggerable wrapping a triggerable matching the provided one.
            // Since Triggerable.equals() doesn't have into account contexts or targets, the
            // safest thing to do would be to create a new quick triggerable for the incoming
            // triggerable, but we could cover both triggerables if we compute the intersection
            // between their respective context and reuse the one we've already stored.
            existingQuickTriggerable.intersectContextWith(triggerable);
            return existingQuickTriggerable.getTriggerable();
        }

        QuickTriggerable newQuickTriggerable = QuickTriggerable.of(triggerable);
        allTriggerables.add(newQuickTriggerable);

        // Build the triggerable per trigger index
        Set<TreeReference> triggers = triggerable.getTriggers();
        for (TreeReference trigger : triggers) {
            if (!triggerablesPerTrigger.containsKey(trigger))
                triggerablesPerTrigger.put(trigger, new HashSet<>());
            triggerablesPerTrigger.get(trigger).add(newQuickTriggerable);
        }

        return triggerable;
    }

    /**
     * Finalize the DAG associated with the form's triggered conditions. This
     * will create the appropriate ordering and dependencies to ensure the
     * conditions will be evaluated in the appropriate orders.
     */
    public void finalizeTriggerables(FormInstance mainInstance) throws IllegalStateException {
        List<QuickTriggerable[]> edges = getDagEdges(allTriggerables, triggerablesPerTrigger);
        triggerablesDAG = buildDag(allTriggerables, edges);
        repeatConditionsPerTargets = getRepeatConditionsPerTargets(mainInstance, triggerablesDAG);
    }

    private static Map<TreeReference, QuickTriggerable> getRepeatConditionsPerTargets(FormInstance mainInstance, Set<QuickTriggerable> triggerables) {
        Map<TreeReference, QuickTriggerable> repeatConditionsPerTargets = new HashMap<>();
        for (QuickTriggerable triggerable : triggerables)
            if (triggerable.isCondition())
                for (TreeReference target : triggerable.getTargets())
                    if (mainInstance.getTemplate(target) != null)
                        repeatConditionsPerTargets.put(target, triggerable);
        return repeatConditionsPerTargets;
    }

    // TODO We can avoid having to resolve dependant refs using the mainInstance by adding descendant refs as targets of relevance triggerables

    /**
     * Returns the list of edges in the DAG that can be built from the provided vertices.
     * <p>
     * This method has side-effects:
     * <ul>
     *     <li>Throws IllegalStateException when cycles are detected, either due to a self-reference or more complex cycle chains</li>
     *     <li>Builds a cache of immediate cascades of each vertex, meaning that we will remember the dependant vertices without having to traverse the DAG again</li>
     * </ul>
     */
    private static List<QuickTriggerable[]> getDagEdges(Set<QuickTriggerable> vertices, Map<TreeReference, Set<QuickTriggerable>> triggerIndex) {
        List<QuickTriggerable[]> edges = new ArrayList<>();
        for (QuickTriggerable vertex : vertices) {
            Set<QuickTriggerable> dependants = getDependantTriggerables(vertex, triggerIndex);

            if (dependants.contains(vertex))
                throwCyclesInDagException(dependants);

            if (vertex.canCascade())
                for (QuickTriggerable dependantVertex : dependants)
                    edges.add(new QuickTriggerable[]{vertex, dependantVertex});

            // TODO Move this from Triggerable to TriggerableDag
            vertex.setImmediateCascades(dependants);
        }
        return edges;
    }

    private static Set<QuickTriggerable> buildDag(Set<QuickTriggerable> vertices, List<QuickTriggerable[]> edges) {
        Set<QuickTriggerable> dag = new LinkedHashSet<>();

        Set<QuickTriggerable> remainingVertices = new HashSet<>(vertices);
        Set<QuickTriggerable[]> remainingEdges = new HashSet<>(edges);
        while (remainingVertices.size() > 0) {
            Set<QuickTriggerable> roots = new HashSet<>(remainingVertices);
            for (QuickTriggerable[] edge : remainingEdges)
                roots.remove(edge[1]);

            if (roots.size() == 0)
                throwCyclesInDagException(vertices);

            dag.addAll(roots);

            Set<QuickTriggerable> newRemainingVertices = new HashSet<>();
            for (QuickTriggerable vertex : vertices)
                if (!dag.contains(vertex))
                    newRemainingVertices.add(vertex);
            remainingVertices = newRemainingVertices;

            Set<QuickTriggerable[]> newRemainingEdges = new HashSet<>();
            for (QuickTriggerable[] edge : remainingEdges)
                if (!roots.contains(edge[0]))
                    newRemainingEdges.add(edge);
            remainingEdges = newRemainingEdges;
        }
        return dag;
    }

    private static void throwCyclesInDagException(Collection<QuickTriggerable> triggerables) {
        StringBuilder hints = new StringBuilder();
        for (QuickTriggerable qt : triggerables) {
            for (TreeReference r : qt.getTargets()) {
                hints.append("\n").append(r.toString(true));
            }
        }
        String message = "Cycle detected in form's relevant and calculation logic!";
        if (!hints.toString().equals("")) {
            message += "\nThe following nodes are likely involved in the loop:"
                + hints;
        }
        throw new IllegalStateException(message);
    }

    /**
     * Get all of the elements which will need to be evaluated (in order) when
     * the triggerable is fired.
     */
    private static Set<QuickTriggerable> getDependantTriggerables(QuickTriggerable triggerable, Map<TreeReference, Set<QuickTriggerable>> triggerIndex) {
        Set<QuickTriggerable> result = new HashSet<>();
        for (TreeReference targetRef : triggerable.getTargets()) {
            Set<QuickTriggerable> children = triggerIndex.containsKey(targetRef)
                ? triggerIndex.get(targetRef)
                : emptySet();
            result.addAll(children);
            for (QuickTriggerable child : children)
                if (child != triggerable && !result.contains(child))
                    result.addAll(getDependantTriggerables(child, triggerIndex));
        }
        return result;
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
                for (QuickTriggerable qu : qt.getImmediateCascades()) {
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
            if (event == FormEntryController.EVENT_QUESTION) {
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
        Set<QuickTriggerable> triggered = triggerablesPerTrigger.get(genericRef);
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

    protected final void publishSummary(String lead, TreeReference ref, Collection<QuickTriggerable> quickTriggerables) {
        accessor.getEventNotifier().publishEvent(new Event(lead + ": " + (ref != null ? ref.toShortString() + ": " : "") + quickTriggerables.size() + " triggerables were fired."));
    }

    public void reportDependencyCycles() {
        Set<TreeReference> vertices = new HashSet<>();
        List<TreeReference[]> edges = new ArrayList<>();

        //build graph
        List<TreeReference> targets = new ArrayList<>();
        for (TreeReference trigger : triggerablesPerTrigger.keySet()) {
            vertices.add(trigger);
            Set<QuickTriggerable> triggered = triggerablesPerTrigger.get(trigger);
            targets.clear();
            for (QuickTriggerable qt : triggered) {
                for (TreeReference target : qt.getTargets()) {
                    if (!targets.contains(target))
                        targets.add(target);
                }
            }

            for (TreeReference target : targets) {
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
            for (TreeReference[] edge : edges) {
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
            for (TreeReference[] edge : edges) {
                b.append(edge[0].toString()).append(" => ").append(edge[1].toString()).append("\n");
            }
            logger.error("XForm Parse Error: {}", b.toString());

            throw new RuntimeException("Dependency cycles amongst the xpath expressions in relevant/calculate");
        }
    }

    // region External Serialization

    public void writeExternalTriggerables(DataOutputStream dos) throws IOException {
        // Order of writes must match order of reads in readExternalTriggerables
        ExtUtil.write(dos, new ExtWrapList(getConditions()));
        ExtUtil.write(dos, new ExtWrapList(getRecalculates()));
    }

    @SuppressWarnings("unchecked")
    public static List<Triggerable> readExternalTriggerables(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
        // Order of reads must match order of writes in writeExternalTriggerables
        List<Triggerable> triggerables = new LinkedList<>();
        triggerables.addAll((List<Triggerable>) ExtUtil.read(dis, new ExtWrapList(Condition.class), pf));
        triggerables.addAll((List<Triggerable>) ExtUtil.read(dis, new ExtWrapList(Recalculate.class), pf));
        return triggerables;
    }

    private List<Condition> getConditions() {
        List<Condition> conditions = new ArrayList<>();
        for (QuickTriggerable qt : allTriggerables) {
            if (qt.isCondition()) {
                conditions.add((Condition) qt.getTriggerable());
            }
        }
        return conditions;
    }

    private List<Recalculate> getRecalculates() {
        List<Recalculate> recalculates = new ArrayList<>();
        for (QuickTriggerable qt : allTriggerables) {
            if (qt.isRecalculate()) {
                recalculates.add((Recalculate) qt.getTriggerable());
            }
        }
        return recalculates;
    }

    // endregion

}
