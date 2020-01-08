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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    /**
     * NOT VALID UNTIL finalizeTriggerables() is called!!
     * <p>
     * Topologically ordered list, meaning that for any tA and tB in
     * the list, where tA comes before tB, evaluating tA cannot depend on any
     * result from evaluating tB.
     */
    protected Set<QuickTriggerable> triggerablesDAG = new LinkedHashSet<>();

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
    protected final Map<TreeReference, Set<QuickTriggerable>> triggerIndex = new HashMap<>();

    /**
     * List of all the triggerables in the form. Unordered.
     */
    protected final Set<QuickTriggerable> unorderedTriggerables = new HashSet<>();

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
        return conditionRepeatTargetIndex.get(repeatRef.genericize());
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
        for (QuickTriggerable qt : unorderedTriggerables) {
            if (qt.contains(t)) {
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

            // TODO It's fishy to mutate the Triggerable here. We might prefer to return a copy of the original Triggerable
            return qt.changeContextRefToIntersectWithTriggerable(t);

            // note, if the contextRef is unnecessarily deep, the condition will be
            // evaluated more times than needed
            // perhaps detect when 'identical' condition has a shorter contextRef,
            // and use that one instead?

        } else {
            qt = QuickTriggerable.of(t);
            unorderedTriggerables.add(qt);

            Set<TreeReference> triggers = t.getTriggers();
            for (TreeReference trigger : triggers) {
                Set<QuickTriggerable> triggered = triggerIndex.get(trigger);
                if (triggered == null) {
                    triggered = new HashSet<>();
                    triggerIndex.put(trigger.clone(), triggered);
                }
                triggered.add(qt);
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
        Set<QuickTriggerable> newTriggerablesDAG = new LinkedHashSet<>();
        // TODO Study how this algorithm ensures that we follow the ancestor >>> descendant direction and if the sorting step is strictly required

        // DAGify the triggerables based on dependencies and sort them so that
        // triggerables come only after the triggerables they depend on
        List<QuickTriggerable> vertices = new ArrayList<>(unorderedTriggerables);
        List<QuickTriggerable[]> edges = new ArrayList<>();
        for (QuickTriggerable triggerable : vertices) {
            Set<QuickTriggerable> dependantTriggerables = getDependantTriggerables(mainInstance, evalContext, triggerable);

            if (dependantTriggerables.contains(triggerable))
                throwCyclesInDagException(dependantTriggerables);

            if (triggerable.canCascade())
                for (QuickTriggerable dependantTriggerable : dependantTriggerables) {
                    edges.add(new QuickTriggerable[]{triggerable, dependantTriggerable});
                }

            // TODO Move this from Triggerable to TriggerableDag
            triggerable.setImmediateCascades(dependantTriggerables);
        }

        List<QuickTriggerable> orderedRoots = new ArrayList<>();
        Set<QuickTriggerable> roots = new HashSet<>(vertices.size());
        while (vertices.size() > 0) {
            // determine root nodes
            roots.clear();
            roots.addAll(vertices);
            for (QuickTriggerable[] edge : edges) {
                roots.remove(edge[1]);
            }

            // if no root nodes while graph still has nodes, graph has cycles
            if (roots.size() == 0)
                throwCyclesInDagException(vertices);

            // order the root nodes - so the order is fixed
            orderedRoots.clear();
            orderedRoots.addAll(roots);
            // TODO Study if insertion order & using LinkedSets would suffice to ensure proper triggerable chaining
            Collections.sort(orderedRoots, QuickTriggerableComparator.INSTANCE);

            // remove root nodes and edges originating from them
            // add them to the triggerablesDAG.
            for (QuickTriggerable root : orderedRoots) {
                newTriggerablesDAG.add(root);
                vertices.remove(root);
            }
            for (int i = edges.size() - 1; i >= 0; i--) {
                QuickTriggerable[] edge = edges.get(i);
                if (roots.contains(edge[0]))
                    edges.remove(i);
            }
        }

        triggerablesDAG.clear();
        triggerablesDAG = newTriggerablesDAG;

        //
        // build the condition index for repeatable nodes
        //
        conditionRepeatTargetIndex.clear();
        for (QuickTriggerable qt : triggerablesDAG) {
            if (qt.isCondition()) {
                for (TreeReference target : qt.getTargets()) {
                    if (mainInstance.getTemplate(target) != null) {
                        conditionRepeatTargetIndex.put(target, qt);
                    }
                }
            }
        }
    }

    public void throwCyclesInDagException(Collection<QuickTriggerable> vertices) {
        StringBuilder hints = new StringBuilder();
        for (QuickTriggerable qt2 : vertices) {
            for (TreeReference r : qt2.getTargets()) {
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
    public Set<QuickTriggerable> getDependantTriggerables(FormInstance mainInstance, EvaluationContext ec, QuickTriggerable triggerable) {
        Set<QuickTriggerable> allDependantTriggerables = new LinkedHashSet<>();
        Set<TreeReference> targets = new HashSet<>();
        for (TreeReference target : triggerable.getTargets()) {

            targets.add(target);

            // For certain types of triggerables, the update will affect
            // not only the target, but also the children of the target.
            // In that case, we want to add all of those nodes
            // to the list of updated elements as well.
            if (triggerable.isCascadingToChildren()) {
                targets.addAll(getChildrenOfReference(mainInstance, ec, target));
            }
        }

        // Now go through each of these updated nodes (generally
        // just 1 for a normal calculation,
        // multiple nodes if there's a relevance cascade.
        for (TreeReference target : targets) {
            // Check our index to see if that target is a Trigger
            // for other conditions
            // IE: if they are an element of a different calculation
            // or relevancy calc

            // We can't make this reference generic before now or
            // we'll lose the target information,
            // so we'll be more inclusive than needed and see if any
            // of our triggers are keyed on the predicate-less path
            // of this ref
            Set<QuickTriggerable> dependantTriggerables = triggerIndex.get(target.hasPredicates() ? target.removePredicates() : target);
            if (dependantTriggerables != null)
                allDependantTriggerables.addAll(dependantTriggerables);
        }
        return allDependantTriggerables;
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
     * This is a utility method to get all of the references of a node. It can
     * be replaced when we support dependent XPath Steps (IE: /path/to//)
     *
     * @return
     */
    private Set<TreeReference> getChildrenOfReference(FormInstance mainInstance, EvaluationContext ec, TreeReference ref) {
        TreeElement repeatTemplate = mainInstance.getTemplatePath(ref);
        if (repeatTemplate != null)
            return getChildrenRefsByTemplate(mainInstance, repeatTemplate);
        // TODO Write a test that goes through this path and see how and why childrenRef can be different than child.getRef()
        return getChildrenByExpandedRef(mainInstance, ec, ref);
    }

    private Set<TreeReference> getChildrenRefsOfElement(FormInstance mainInstance, AbstractTreeElement<?> element) {
        TreeElement repeatTemplate = mainInstance.getTemplatePath(element.getRef());
        if (repeatTemplate != null)
            return getChildrenRefsByTemplate(mainInstance, repeatTemplate);
        return getChildrenOfElement(mainInstance, element);
    }

    private Set<TreeReference> getChildrenRefsByTemplate(FormInstance mainInstance, TreeElement repeatTemplate) {
        return getChildrenOfElement(mainInstance, repeatTemplate);
    }

    private Set<TreeReference> getChildrenByExpandedRef(FormInstance mainInstance, EvaluationContext ec, TreeReference initialRef) {
        Set<TreeReference> descendants = new HashSet<>();
        for (TreeReference childrenRef : ec.expandReference(initialRef)) {
            AbstractTreeElement<?> child = ec.resolveReference(childrenRef);
            descendants.add(child.getRef().genericize());
            descendants.addAll(getChildrenRefsOfElement(mainInstance, child));
        }
        return descendants;
    }

    private Set<TreeReference> getChildrenOfElement(FormInstance mainInstance, AbstractTreeElement<?> element) {
        Set<TreeReference> childrenRefs = new HashSet<>();
        for (int i = 0; i < element.getNumChildren(); ++i) {
            AbstractTreeElement<?> child = element.getChildAt(i);
            childrenRefs.add(child.getRef().genericize());
            childrenRefs.addAll(getChildrenRefsOfElement(mainInstance, child));
        }
        return childrenRefs;
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
        Set<QuickTriggerable> triggered = triggerIndex.get(genericRef);
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
        for (TreeReference trigger : triggerIndex.keySet()) {
            vertices.add(trigger);
            Set<QuickTriggerable> triggered = triggerIndex.get(trigger);
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
        for (QuickTriggerable qt : unorderedTriggerables) {
            if (qt.isCondition()) {
                conditions.add((Condition) qt.getTriggerable());
            }
        }
        return conditions;
    }

    private List<Recalculate> getRecalculates() {
        List<Recalculate> recalculates = new ArrayList<>();
        for (QuickTriggerable qt : unorderedTriggerables) {
            if (qt.isRecalculate()) {
                recalculates.add((Recalculate) qt.getTriggerable());
            }
        }
        return recalculates;
    }

    // endregion

}
