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

import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.PredicateFilter;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

public class TriggerableDag {
    private static final Logger logger = LoggerFactory.getLogger(TriggerableDag.class);

    public interface EventNotifierAccessor {
        EventNotifier getEventNotifier();
    }

    /**
     * Stores the event notifier required to create instances of this class.
     * <p>
     * The event notifier is used to publish events that serve as a form for
     * loose coupling between processes that could be running at the same time
     * in a JavaRosa client.
     */
    private final EventNotifierAccessor accessor;

    /**
     * Stores the unsorted set of all triggerables present in the form.
     * <p>
     * This set is used during the DAG build process.
     */
    private final Set<QuickTriggerable> allTriggerables = new HashSet<>();

    /**
     * Stores the sorted set of all triggerables using the dependency direction
     * as ordering.
     * <p>
     * Triggerables present in this set depend exclusively on preceding
     * triggerables.
     */
    private Set<QuickTriggerable> triggerablesDAG = emptySet();

    /**
     * Stores an index to resolve triggerables by their corresponding trigger's
     * reference.
     * <p>
     * Note that there's a m:n relationship between trigger references and
     * triggerables.
     */
    private final Map<TreeReference, Set<QuickTriggerable>> triggerablesPerTrigger = new HashMap<>();

    /**
     * An index to look up relevance conditions for each repeat. See buildRelevancePerRepeat.
     */
    private Map<TreeReference, QuickTriggerable> relevancePerRepeat = new HashMap<>();

    private boolean predicateCaching = true;
    private PredicateFilter predicateIndex = new IndexingPredicateFilter();

    TriggerableDag(EventNotifierAccessor accessor) {
        this.accessor = accessor;
    }

    //region Creation
    /**
     * Adds the provided triggerable to the DAG.
     * <p>
     * This method has side effects:
     * <ul>
     *     <li>If a similar triggerable has been already added, its context gets
     *     intersected with the provided triggerable to cover both using only
     *     one entry</li>
     *     <li>This method builds the index of triggerables per trigger ref
     *     at {@link #triggerablesPerTrigger}</li>
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

    private QuickTriggerable findTriggerable(Triggerable t) {
        for (QuickTriggerable qt : allTriggerables) {
            if (qt.contains(t)) {
                return qt;
            }
        }
        return null;
    }

    /**
     * Finalize the DAG associated with the form's triggered conditions. This
     * will create the appropriate ordering and dependencies to ensure the
     * conditions will be evaluated in the appropriate orders.
     */
    void finalizeTriggerables(FormInstance mainInstance, EvaluationContext ec) throws IllegalStateException {
        triggerablesDAG = buildDag(allTriggerables, getDagEdges(mainInstance, ec));
        relevancePerRepeat = buildRelevancePerRepeat(mainInstance, triggerablesDAG);
    }

    /**
     * Returns the list of edges in the DAG that can be built from all the
     * triggerables added to the DAG while parsing the form.
     * <p>
     * This method has side effects:
     * <ul>
     *     <li>Throws IllegalStateException when cycles are detected involving
     *     self-references</li>
     *     <li>Builds a cache of immediate cascades of each vertex, meaning that
     *     we will remember the dependant vertices without having to traverse
     *     the DAG again {@link Triggerable#getImmediateCascades()}</li>
     * </ul>
     */
    private Set<QuickTriggerable[]> getDagEdges(FormInstance mainInstance, EvaluationContext ec) {
        Set<QuickTriggerable[]> edges = new HashSet<>();
        for (QuickTriggerable source : allTriggerables) {
            // Compute the set of edge targets from the source vertex in this
            // loop using the triggerable's target tree reference set.
            // We will create an edge for all the source's target references
            // that, in turn, trigger another triggerable.
            Set<QuickTriggerable> targets = getDependantTriggerables(mainInstance, ec, source, triggerablesPerTrigger);

            // Account for cycles by self-reference
            if (targets.contains(source))
                throwCyclesInDagException(targets);

            for (QuickTriggerable target : targets)
                edges.add(new QuickTriggerable[]{source, target});

            // TODO Move this from Triggerable to TriggerableDag
            source.setImmediateCascades(targets);
        }
        return edges;
    }

    private static Set<QuickTriggerable> getDependantTriggerables(FormInstance mainInstance, EvaluationContext ec, QuickTriggerable triggerable, Map<TreeReference, Set<QuickTriggerable>> triggerIndex) {
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

    private static Set<TreeReference> getChildrenOfReference(FormInstance mainInstance, EvaluationContext evalContext, TreeReference original) {
        // original has already been added to the 'toAdd' list.

        Set<TreeReference> descendantRefs = new HashSet<>();
        TreeElement repeatTemplate = mainInstance.getTemplatePath(original);
        if (repeatTemplate != null) {
            for (int i = 0; i < repeatTemplate.getNumChildren(); ++i) {
                TreeElement child = repeatTemplate.getChildAt(i);
                descendantRefs.add(child.getRef().genericize());
                descendantRefs.addAll(getChildrenRefsOfElement(mainInstance, child));
            }
        } else {
            List<TreeReference> refSet = evalContext.expandReference(original);
            for (TreeReference ref : refSet) {
                descendantRefs.addAll(getChildrenRefsOfElement(mainInstance, evalContext.resolveReference(ref)));
            }
        }
        return descendantRefs;
    }

    // Recursive step of utility method
    private static Set<TreeReference> getChildrenRefsOfElement(FormInstance mainInstance, AbstractTreeElement<?> el) {
        Set<TreeReference> childrenRefs = new HashSet<>();
        TreeElement repeatTemplate = mainInstance.getTemplatePath(el.getRef());
        if (repeatTemplate != null) {
            for (int i = 0; i < repeatTemplate.getNumChildren(); ++i) {
                TreeElement child = repeatTemplate.getChildAt(i);
                childrenRefs.add(child.getRef().genericize());
                childrenRefs.addAll(getChildrenRefsOfElement(mainInstance, child));
            }
        } else {
            for (int i = 0; i < el.getNumChildren(); ++i) {
                AbstractTreeElement<?> child = el.getChildAt(i);
                childrenRefs.add(child.getRef().genericize());
                childrenRefs.addAll(getChildrenRefsOfElement(mainInstance, child));
            }
        }
        return childrenRefs;
    }

    /**
     * Returns a set with the DAG that can be build using the provided vertices
     * and edges.
     * <p>
     * This method has side effects:
     * <ul>
     *     <li>Throws IllegalStateException when cycles are detected involving
     *     more than one node</li>
     * </ul>
     */
    private static Set<QuickTriggerable> buildDag(Set<QuickTriggerable> vertices, Set<QuickTriggerable[]> edges) {
        // The dag and the set of remaining vertices will be mutated
        // inside the while loop's block
        Set<QuickTriggerable> dag = new LinkedHashSet<>();
        Set<QuickTriggerable> remainingVertices = new HashSet<>(vertices);

        // The set of remaining edges will be replaced inside
        // the while loop's block
        Set<QuickTriggerable[]> remainingEdges = new HashSet<>(edges);

        while (remainingVertices.size() > 0) {
            // Compute the set of roots (nodes that don't show up
            // as edge targets) with the remaining vertices
            Set<QuickTriggerable> roots = new HashSet<>(remainingVertices);
            for (QuickTriggerable[] edge : remainingEdges)
                roots.remove(edge[1]);

            if (roots.size() == 0)
                throwCyclesInDagException(vertices);

            // "Move" the roots detected during this iteration
            // from the remainingVertices to the DAG
            remainingVertices.removeAll(roots);
            dag.addAll(roots);

            // Compute the new set of remaining edges to continue the iteration
            Set<QuickTriggerable[]> newRemainingEdges = new HashSet<>();
            for (QuickTriggerable[] edge : remainingEdges)
                if (!roots.contains(edge[0]))
                    newRemainingEdges.add(edge);
            remainingEdges = newRemainingEdges;
        }
        return dag;
    }
    //endregion

    //region Validation
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

    void reportDependencyCycles() {
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

    /**
     * Invoked to validate a filled-in form. Sweeps through from beginning to
     * end, confirming that the entered values satisfy all constraints. The
     * FormEntryController is based upon the FormDef, but has its own model and
     * controller independent of anything at the UI layer.
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
    //endregion

    //region Initialization
    /**
     * Walks the current set of conditions, and evaluates each of them with the
     * current context.
     */
    Collection<QuickTriggerable> initializeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference rootRef) {
        return initializeTriggerables(mainInstance, evalContext, rootRef, new HashSet<>());
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

        Set<QuickTriggerable> toTrigger = getAllToTrigger(applicable);
        return doEvaluateTriggerables(mainInstance, evalContext, toTrigger, rootRef, new HashSet<>(), alreadyEvaluated);
    }
    //endregion

    //region Evaluation
    /**
     * The entry point for the DAG cascade after a value is changed in the
     * model.
     *
     * @param changedRef The full contextualized unambiguous reference of the value
     *            that was changed.
     */
    Collection<QuickTriggerable> triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference changedRef) {
        return triggerTriggerables(mainInstance, evalContext, changedRef, new HashSet<>(), new HashSet<>());
    }

    /**
     * Step 2 in evaluating DAG computation updates from a value being changed in the instance. Identifies all triggerables to be evaluated and
     * evaluates them.
     */
    private Set<QuickTriggerable> triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference changedRef, Set<QuickTriggerable> affectAllRepeatInstances, Set<QuickTriggerable> alreadyEvaluated) {
        // The DAG uses generic references as keys
        TreeReference genericRef = changedRef.genericize();

        Set<QuickTriggerable> cascadeRoots = triggerablesPerTrigger.get(genericRef);
        if (cascadeRoots == null) {
            return alreadyEvaluated;
        }

        Set<QuickTriggerable> toTrigger = getAllToTrigger(cascadeRoots);
        return doEvaluateTriggerables(mainInstance, evalContext, toTrigger, changedRef, affectAllRepeatInstances, alreadyEvaluated);
    }

    /**
     * Given a set of cascade roots, return a set of all triggerables across those cascades.
     *
     *  @param cascadeRoots  The roots of the triggerable cascades that must be triggered. Guaranteed not to be modified.
     */
    private Set<QuickTriggerable> getAllToTrigger(Set<QuickTriggerable> cascadeRoots) {
        Set<QuickTriggerable> refSet = new HashSet<>(cascadeRoots);
        Set<QuickTriggerable> toTrigger = new HashSet<>(cascadeRoots);

        while (!refSet.isEmpty()) {
            Set<QuickTriggerable> newSet = new HashSet<>();
            for (QuickTriggerable qt : refSet) {
                // Leverage the saved DAG edges. This may over-fill the set of triggerables but should be faster than
                // recomputing the edges. With value-change optimizations, this should be much faster.
                for (QuickTriggerable qu : qt.getImmediateCascades()) {
                    if (!toTrigger.contains(qu)) {
                        toTrigger.add(qu);
                        newSet.add(qu);
                    }
                }
            }
            refSet = newSet;
        }

        return toTrigger;
    }

    private Set<QuickTriggerable> doEvaluateTriggerables(FormInstance mainInstance, EvaluationContext evalContext, Set<QuickTriggerable> toTrigger,
                                                         TreeReference changedRef, Set<QuickTriggerable> affectAllRepeatInstances, Set<QuickTriggerable> alreadyEvaluated) {
        Set<QuickTriggerable> evaluated = new HashSet<>();

        EvaluationContext context;
        if (predicateCaching) {
            context = new EvaluationContext(evalContext, Arrays.asList(
                predicateIndex,
                new IdempotentInMemPredicateCache()
            ));
        } else {
            context = evalContext;
        }

        // Evaluate the provided set of triggerables in the order they appear
        // in the sorted DAG to ensure the correct sequence of evaluations
        for (QuickTriggerable qt : triggerablesDAG)
            if (toTrigger.contains(qt) && !alreadyEvaluated.contains(qt)) {
                evaluateTriggerable(mainInstance, context, qt, affectAllRepeatInstances.contains(qt), changedRef);

                evaluated.add(qt);
            }

        return evaluated;
    }

    /**
     * Step 3 in DAG cascade. Evaluate the individual triggerable expressions.
     */
    private void evaluateTriggerable(FormInstance mainInstance, EvaluationContext evalContext, QuickTriggerable toTrigger, boolean affectsAllRepeatInstances, TreeReference changedRef) {
        // For addition or removal of repeat instances, contextualizing against the changed ref ensures that triggerables with triggers and targets inside
        // the repeat are only triggered for the changed instance. This is important for performance.
        TreeReference contextRef = affectsAllRepeatInstances ? toTrigger.getContext() : toTrigger.getContext().contextualize(changedRef);

        // In general, expansion will have no effect. It only makes a difference if affectsAllRepeatInstances is true in
        // which case the triggerable will be applied for every repeat instance.
        List<TreeReference> qualifiedReferences = evalContext.expandReference(contextRef);

        List<EvaluationResult> evaluationResults = new ArrayList<>(0);
        for (TreeReference qualified : qualifiedReferences) {
            try {
                // apply evaluates the expression in the given context and saves the result in the contextualized target(s).
                evaluationResults.addAll(toTrigger.apply(mainInstance, new EvaluationContext(evalContext, qualified), qualified));
            } catch (Exception e) {
                throw new RuntimeException("Error evaluating field '" + contextRef.getNameLast() + "' (" + qualified + "): " + e.getMessage(), e);
            }
        }

        if (evaluationResults.size() > 0) {
            accessor.getEventNotifier().publishEvent(new Event(toTrigger.isCondition() ? "Condition" : "Recalculate", evaluationResults));
        }
    }

    private void evaluateChildrenTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeElement newNode, boolean createdOrDeleted, Set<QuickTriggerable> alreadyEvaluated) {
        // iterate into the group children and evaluate any triggerables that
        // depend on them, if they have not already been calculated.
        int numChildren = newNode.getNumChildren();
        for (int i = 0; i < numChildren; i++) {
            TreeReference anchorRef = newNode.getChildAt(i).getRef();
            Set<QuickTriggerable> childTriggerables = triggerTriggerables(mainInstance, evalContext, anchorRef, new HashSet<>(), alreadyEvaluated);
            publishSummary((createdOrDeleted ? "Created" : "Deleted"), anchorRef, childTriggerables);
        }
    }
    //endregion

    //region Repeat instance creation and deletion
    void createRepeatInstance(FormInstance mainInstance, EvaluationContext evalContext, TreeReference createdRef, TreeElement createdElement) {
        Set<QuickTriggerable> affectAllInstances = getTriggerablesAffectingAllInstances(createdRef.genericize());

        // trigger conditions that depend on the creation of this new node
        Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, createdRef, affectAllInstances, new HashSet<>(0));
        publishSummary("Created (phase 1)", createdRef, qtSet1);

        // initialize conditions for the node (and sub-nodes)
        Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, createdRef, new HashSet<>());
        publishSummary("Created (phase 2)", createdRef, qtSet2);

        Set<QuickTriggerable> alreadyEvaluated = new HashSet<>(qtSet1);
        alreadyEvaluated.addAll(qtSet2);

        // TODO: add a test that fails without this or remove (all tests on v2.17 pre-DAG simplification pass)
        evaluateChildrenTriggerables(mainInstance, evalContext, createdElement, true, alreadyEvaluated);
    }

    void deleteRepeatInstance(FormInstance mainInstance, EvaluationContext evalContext, TreeReference deleteRef, TreeElement deletedElement) {
        Set<QuickTriggerable> affectAllInstances = getTriggerablesAffectingAllInstances(deleteRef.genericize());

        Set<QuickTriggerable> alreadyEvaluated = triggerTriggerables(mainInstance, evalContext, deleteRef, affectAllInstances, new HashSet<>());
        evaluateChildrenTriggerables(mainInstance, evalContext, deletedElement, false, alreadyEvaluated);
    }

    /**
     * Returns triggerables with expressions that will need to be updated for every instance of the
     * repeat at the given reference. There are two cases considered:
     * <ul>
     *   <li>triggerables in the context of the given repeat that are triggered directly by that repeat or are in a cascade
     *       triggered directly by the repeat. For example, if the repeat reference is /data/repeat, a triggerable representing
     *       a calculate at /data/repeat/calc with an expression such as position(..) would be included.
     *   </li>
     *   <li>triggerables in the context of the given repeat that have a trigger outside the repeat. For example, if there is a
     *       count of repeat instances outside the repeat, a triggerable representing a calculate inside the repeat with an
     *       expression that references the count will be included.
     *   </li>
     * </ul>
     *
     * This omits one case: references within a repeat instance to another repeat instance.
     */
    private Set<QuickTriggerable> getTriggerablesAffectingAllInstances(TreeReference genericRepeatRef) {
        Set<QuickTriggerable> result = new HashSet<>();
        Set<QuickTriggerable> cascadeRoots = triggerablesPerTrigger.get(genericRepeatRef);
        Set<QuickTriggerable> outsideRepeat = new HashSet<>();

        if (cascadeRoots != null) {
            for (QuickTriggerable root : cascadeRoots) {
                if (genericRepeatRef.isAncestorOf(root.getContext(), false)) {
                    result.add(root);
                    // the loop below ensures every triggerable in the cascade is added
                }
            }

            Set<QuickTriggerable> toConsider = new HashSet<>(cascadeRoots);
            while (!toConsider.isEmpty()) {
                Set<QuickTriggerable> nextCascadeLevel = new HashSet<>();
                for (QuickTriggerable qt : toConsider) {
                    if (!genericRepeatRef.isAncestorOf(qt.getContext(), true)) {
                        outsideRepeat.add(qt);
                    } else {
                        Set<QuickTriggerable> parentsToConsider = new HashSet<>(outsideRepeat);
                        parentsToConsider.addAll(result);
                        for (QuickTriggerable parent : parentsToConsider) {
                            if (parent.getImmediateCascades().contains(qt)) {
                                result.add(qt);
                            }
                        }
                    }

                    nextCascadeLevel.addAll(qt.getImmediateCascades());
                }
                toConsider = nextCascadeLevel;
            }
        }

        return result;
    }

    /**
     * Builds an index to look up relevance for repeats. Assumes that there can only be a single Triggerable associated
     * with a repeat and that it must represent relevance. This Triggerable is evaluated directly to determine relevance
     * at a new repeat prompt. This is used instead of the relevance stored in the corresponding TreeElement and updated
     * by the DAG because repeat templates aren't updated by DAG recomputations. Additionally, the context intersection
     * made in Triggerable.intersectContextWith means we may not evaluate against the correct context when the triggerable is
     * applied.
     */
    private static Map<TreeReference, QuickTriggerable> buildRelevancePerRepeat(FormInstance mainInstance, Set<QuickTriggerable> triggerables) {
        Map<TreeReference, QuickTriggerable> relevancePerRepeat = new HashMap<>();
        for (QuickTriggerable triggerable : triggerables)
            if (triggerable.isCondition())
                for (TreeReference target : triggerable.getTargets())
                    if (mainInstance.getTemplate(target) != null)
                        relevancePerRepeat.put(target, triggerable);
        return relevancePerRepeat;
    }

    public QuickTriggerable getRelevanceForRepeat(TreeReference genericRepeatRef) {
        return relevancePerRepeat.get(genericRepeatRef);
    }

    void copyItemsetAnswer(FormInstance mainInstance, EvaluationContext evalContext, TreeReference copyRef, TreeElement copyToElement) {
        TreeReference targetRef = copyToElement.getRef();

        // trigger conditions that depend on the creation of these new nodes
        Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance, evalContext, copyRef, new HashSet<>(), new HashSet<>());

        publishSummary("Copied itemset answer (phase 1)", targetRef, qtSet1);

        // initialize conditions for the node (and sub-nodes)
        Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance, evalContext, copyRef, qtSet1);
        publishSummary("Copied itemset answer (phase 2)", targetRef, qtSet2);
        // not 100% sure this will work since destRef is ambiguous as the last
        // step, but i think it's supposed to work
    }

    final void publishSummary(String lead, TreeReference ref, Collection<QuickTriggerable> quickTriggerables) {
        accessor.getEventNotifier().publishEvent(new Event(lead + ": " + (ref != null ? ref.toShortString() + ": " : "") + quickTriggerables.size() + " triggerables were fired."));
    }

    // region External Serialization

    void writeExternalTriggerables(DataOutputStream dos) throws IOException {
        // Order of writes must match order of reads in readExternalTriggerables
        ExtUtil.write(dos, new ExtWrapList(getConditions()));
        ExtUtil.write(dos, new ExtWrapList(getRecalculates()));
    }

    @SuppressWarnings("unchecked")
    static List<Triggerable> readExternalTriggerables(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
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

    public void disablePredicateCaching() {
        this.predicateCaching = false;
    }
}
