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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

/**
 * The fast (Latest_fastest) eval logic for 2014
 *
 * @author mitchellsundt@gmail.com
 * @author meletis@surveycto.com
 *
 */
public class Fast2014DagImpl extends LatestDagBase {

	private final EvalBehavior mode = EvalBehavior.Fast_2014;

	public Fast2014DagImpl(EventNotifierAccessor accessor) {
		super(accessor);
	}

	@Override
	public EvalBehavior getEvalBehavior() {
		return mode;
	}

	@Override
	public QuickTriggerable getTriggerableForRepeatGroup(TreeReference repeatRef) {
		return conditionRepeatTargetIndex.get(repeatRef.genericize());
	}

	@Override
	public void deleteRepeatGroup(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference deleteRef,
			TreeElement parentElement, TreeElement deletedElement) {

		Set<QuickTriggerable> alreadyEvaluated = triggerTriggerables(
				mainInstance, evalContext, deleteRef, true,
				new HashSet<QuickTriggerable>(0));
		publishSummary("Deleted", deleteRef, alreadyEvaluated);

		evaluateChildrenTriggerables(mainInstance, evalContext, deletedElement,
				false, true, alreadyEvaluated);
	}

	@Override
	public void createRepeatGroup(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference createRef,
			TreeElement parentElement, TreeElement createdElement) {

		// trigger conditions that depend on the creation of this new node
		Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance,
				evalContext, createRef, true, new HashSet<QuickTriggerable>(0));
		publishSummary("Created (phase 1)", createRef, qtSet1);

		// initialize conditions for the node (and sub-nodes)
		Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance,
				evalContext, createRef, true, new HashSet<QuickTriggerable>(0));
		publishSummary("Created (phase 2)", createRef, qtSet2);

		Set<QuickTriggerable> alreadyEvaluated = new HashSet<QuickTriggerable>(
				qtSet1);
		alreadyEvaluated.addAll(qtSet2);

		evaluateChildrenTriggerables(mainInstance, evalContext, createdElement,
				true, true, alreadyEvaluated);
	}

	private void evaluateChildrenTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeElement newNode,
			boolean createdOrDeleted, boolean midSurvey,
			Set<QuickTriggerable> alreadyEvaluated) {
		// iterate into the group children and evaluate any triggerables that
		// depend one them, if they are not already calculated.
		int numChildren = newNode.getNumChildren();
		for (int i = 0; i < numChildren; i++) {
			TreeReference anchorRef = newNode.getChildAt(i).getRef();
			Set<QuickTriggerable> childTriggerables = triggerTriggerables(
					mainInstance, evalContext, anchorRef,
					  midSurvey, alreadyEvaluated);
			publishSummary((createdOrDeleted ? "Created" : "Deleted"),
					anchorRef, childTriggerables);
		}
	}

	@Override
	public void copyItemsetAnswer(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference copyRef,
			TreeElement copyToElement, boolean midSurvey) {

		TreeReference targetRef = copyToElement.getRef();

		Set<QuickTriggerable> qtSet1 = triggerTriggerables(mainInstance,
				evalContext, copyRef, midSurvey,
				new HashSet<QuickTriggerable>(0));// trigger conditions that
													// depend on the creation of
													// these new nodes
		publishSummary("Copied itemset answer (phase 1)", targetRef, qtSet1);

		Set<QuickTriggerable> qtSet2 = initializeTriggerables(mainInstance,
				evalContext, copyRef, midSurvey,
				new HashSet<QuickTriggerable>(0));// initialize conditions for
													// the node (and sub-nodes)
		publishSummary("Copied itemset answer (phase 2)", targetRef, qtSet2);
		// not 100% sure this will work since destRef is ambiguous as the last
		// step, but i think it's supposed to work
	}

	/**
	 * Finalize the DAG associated with the form's triggered conditions. This
	 * will create the appropriate ordering and dependencies to ensure the
	 * conditions will be evaluated in the appropriate orders.
	 *
	 * @throws IllegalStateException
	 *             - If the trigger ordering contains an illegal cycle and the
	 *             triggers can't be laid out appropriately
	 */
	@Override
	public void finalizeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext) throws IllegalStateException {
		//
		// DAGify the triggerables based on dependencies and sort them so that
		// triggerables come only after the triggerables they depend on
		//

		ArrayList<QuickTriggerable> vertices = new ArrayList<QuickTriggerable>(
				unorderedTriggerables);
		triggerablesDAG.clear();

		ArrayList<QuickTriggerable[]> partialOrdering = new ArrayList<QuickTriggerable[]>();
		HashSet<QuickTriggerable> deps = new HashSet<QuickTriggerable>();
		HashSet<QuickTriggerable> newDestinationSet = new HashSet<QuickTriggerable>();
		for (QuickTriggerable qt : vertices) {
			deps.clear();
			newDestinationSet.clear();
			fillTriggeredElements(mainInstance, evalContext, qt, deps,
					newDestinationSet, true);

			// remove any self-reference if we have one...
			deps.remove(qt);
			for (QuickTriggerable qu : deps) {
				QuickTriggerable[] edge = { qt, qu };
				partialOrdering.add(edge);
			}

			// save for aggressive 2014 behavior
			qt.t.setImmediateCascades(deps);
		}

		ArrayList<QuickTriggerable> orderedRoots = new ArrayList<QuickTriggerable>();
		HashSet<QuickTriggerable> roots = new HashSet<QuickTriggerable>(
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

			// order the root nodes - mainly for clean textual diffs when
			// printing
			// When this code is rewritten to use HashSet, this will become
			// important.
			orderedRoots.clear();
			orderedRoots.addAll(roots);
			Collections.sort(orderedRoots,
					QuickTriggerable.quickTriggerablesRootOrdering);

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

		// printTriggerables();
	}

	/**
	 * Get all of the elements which will need to be evaluated (in order) when
	 * the triggerable is fired.
	 * 
	 * @param qt
	 * @param destinationSet
	 *            where to store the triggerables
	 * @param midSurvey
	 *            if true, then the slow code will execute, if false, then
	 *            old/fast code will execute that suffers from
	 *            https://code.google.com/p/opendatakit/issues/detail?id=888
	 */
	public void fillTriggeredElements(FormInstance mainInstance,
			EvaluationContext evalContext, QuickTriggerable qt,
			Set<QuickTriggerable> destinationSet,
			Set<QuickTriggerable> newDestinationSet,
			boolean midSurvey) {
		if (qt.t.canCascade()) {
			List<TreeReference> targets = qt.t.getTargets();
			if (!midSurvey) {
				for (TreeReference target : targets) {
					ArrayList<QuickTriggerable> triggered = triggerIndex
							  .get(target);
					if (triggered != null) {
						for (QuickTriggerable qu : triggered) {
							if (!destinationSet.contains(qu)) {
								destinationSet.add(qu);
								newDestinationSet.add(qu);
							}
						}
					}
				}
			} else {
				boolean expandRepeatables = true;

				for (TreeReference target : targets) {
					Set<TreeReference> updatedNodes = new HashSet<TreeReference>();
					updatedNodes.add(target);

					// For certain types of triggerables, the update will affect
					// not
					// only
					// the target, but
					// also the children of the target. In that case, we want to
					// add
					// all
					// of those nodes
					// to the list of updated elements as well.
					if (qt.t.isCascadingToChildren()) {
						addChildrenOfReference(mainInstance, evalContext, target, updatedNodes,
								  expandRepeatables);
					}

					// Now go through each of these updated nodes (generally
					// just 1
					// for a
					// normal calculation,
					// multiple nodes if there's a relevance cascade.
					for (TreeReference ref : updatedNodes) {
						// Check our index to see if that target is a Trigger
						// for
						// other
						// conditions
						// IE: if they are an element of a different calculation
						// or
						// relevancy calc

						// We can't make this reference generic before now or
						// we'll
						// lose the
						// target information,
						// so we'll be more inclusive than needed and see if any
						// of
						// our
						// triggers are keyed on
						// the predicate-less path of this ref
						ArrayList<QuickTriggerable> triggered = triggerIndex
								.get(ref.hasPredicates() ? ref
										  .removePredicates() : ref);

						if (triggered != null) {
							// If so, walk all of these triggerables that we
							// found
							for (QuickTriggerable qu : triggered) {
								// And add them to the queue if they aren't
								// there
								// already
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
	}

	/**
	 * Step 2 in evaluating DAG computation updates from a value being changed
	 * in the instance. This step is responsible for taking the root set of
	 * directly triggered conditions, identifying which conditions should
	 * further be triggered due to their update, and then dispatching all of the
	 * evaluations.
	 * 
	 * @param tv
	 *            A set of all of the trigerrables directly triggered by the
	 *            value changed
	 * @param anchorRef
	 */
	private Set<QuickTriggerable> evaluateTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext,
			Set<QuickTriggerable> tv, TreeReference anchorRef,
			boolean midSurvey,
			Set<QuickTriggerable> alreadyEvaluated) {
		// add all cascaded triggerables to queue

		// Iterate through all of the currently known triggerables to be
		// triggered
		// Iterate through all of the currently known triggerables to be
		// triggered
		Set<QuickTriggerable> refSet = new HashSet<QuickTriggerable>(tv);
		for (; !refSet.isEmpty();) {
			Set<QuickTriggerable> newSet = new HashSet<QuickTriggerable>();
			for (QuickTriggerable qt : refSet) {
				fillTriggeredElements(mainInstance, evalContext, qt, tv, newSet,
						  midSurvey);
			}
			refSet = newSet;
		}

		return doEvaluateTriggerables(mainInstance, evalContext, tv, anchorRef, alreadyEvaluated);
	}

	/**
	 * This is a utility method to get all of the references of a node. It can
	 * be replaced when we support dependent XPath Steps (IE: /path/to//)
	 */
	public void addChildrenOfReference(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference original,
			Set<TreeReference> toAdd, boolean expandRepeatables) {
		// original has already been added to the 'toAdd' list.

		TreeElement repeatTemplate = expandRepeatables ? mainInstance
				.getTemplatePath(original) : null;
		if (repeatTemplate != null) {
			for (int i = 0; i < repeatTemplate.getNumChildren(); ++i) {
				TreeElement child = repeatTemplate.getChildAt(i);
				toAdd.add(child.getRef().genericize());
				addChildrenOfElement(mainInstance, evalContext, child, toAdd, expandRepeatables);
			}
		} else {
			List<TreeReference> refSet = evalContext
					.expandReference(original);
			for (TreeReference ref : refSet) {
				addChildrenOfElement(mainInstance, evalContext, evalContext.resolveReference(ref),
						toAdd, expandRepeatables);
			}
		}
	}

	// Recursive step of utility method
	private void addChildrenOfElement(FormInstance mainInstance,
			EvaluationContext evalContext, TreeElement el, Set<TreeReference> toAdd,
			boolean expandRepeatables) {
		TreeElement repeatTemplate = expandRepeatables ? mainInstance
				.getTemplatePath(el.getRef()) : null;
		if (repeatTemplate != null) {
			for (int i = 0; i < repeatTemplate.getNumChildren(); ++i) {
				TreeElement child = repeatTemplate.getChildAt(i);
				toAdd.add(child.getRef().genericize());
				addChildrenOfElement(mainInstance, evalContext, child, toAdd, expandRepeatables);
			}
		} else {
			for (int i = 0; i < el.getNumChildren(); ++i) {
				TreeElement child = el.getChildAt(i);
				toAdd.add(child.getRef().genericize());
				addChildrenOfElement(mainInstance, evalContext, child, toAdd, expandRepeatables);
			}
		}
	}

	/**
	 * Walks the current set of conditions, and evaluates each of them with the
	 * current context.
	 */

	@Override
	public Collection<QuickTriggerable> initializeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference rootRef,
			boolean midSurvey) {

		return initializeTriggerables(mainInstance, evalContext, rootRef,
				  midSurvey, new HashSet<QuickTriggerable>(1));
	}

	private Set<QuickTriggerable> initializeTriggerables(
			FormInstance mainInstance, EvaluationContext evalContext,
			TreeReference rootRef, boolean midSurvey,
			Set<QuickTriggerable> alreadyEvaluated) {
		TreeReference genericRoot = rootRef.genericize();

		Set<QuickTriggerable> applicable = new HashSet<QuickTriggerable>();
		for (int i = 0; i < triggerablesDAG.size(); i++) {
			QuickTriggerable qt = triggerablesDAG.get(i);
			for (int j = 0; j < qt.t.getTargets().size(); j++) {
				TreeReference target = qt.t.getTargets().get(j);
				if (genericRoot.isParentOf(target, false)) {
					applicable.add(qt);
					break;
				}
			}
		}

		return evaluateTriggerables(mainInstance, evalContext, applicable,
				rootRef, midSurvey, alreadyEvaluated);
	}

	/**
	 * The entry point for the DAG cascade after a value is changed in the
	 * model.
	 *
	 * @param ref
	 *            The full contextualized unambiguous reference of the value
	 *            that was changed.
	 */
	@Override
	public Collection<QuickTriggerable> triggerTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			boolean midSurvey) {

		return this.triggerTriggerables(mainInstance,
				  evalContext, ref, midSurvey,
				  new HashSet<QuickTriggerable>(1));
	}

	private Set<QuickTriggerable> triggerTriggerables(
			FormInstance mainInstance, EvaluationContext evalContext,
			TreeReference ref, boolean midSurvey,
			Set<QuickTriggerable> alreadyEvaluated) {

		// turn unambiguous ref into a generic ref
		// to identify what nodes should be triggered by this
		// reference changing
		TreeReference genericRef = ref.genericize();

		// get triggerables which are activated by the generic reference
		ArrayList<QuickTriggerable> triggered = triggerIndex.get(genericRef);
		if (triggered == null) {
			return alreadyEvaluated;
		}

		Set<QuickTriggerable> triggeredCopy = new HashSet<QuickTriggerable>(
				triggered);

		// Evaluate all of the triggerables in our new set
		return evaluateTriggerables(mainInstance, evalContext, triggeredCopy,
				ref, midSurvey, alreadyEvaluated);
	}

	@Override
	public boolean shouldTrustPreviouslyCommittedAnswer() {
		return true;
	}
}
