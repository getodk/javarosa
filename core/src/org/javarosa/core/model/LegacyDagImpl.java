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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.javarosa.core.log.WrappedException;
import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

/**
 * The legacy eval logic
 *
 * @author mitchellsundt@gmail.com
 * @author meletis@surveycto.com
 *
 */
public class LegacyDagImpl extends IDag {

   private final EvalBehavior mode = EvalBehavior.Legacy;

   private HashMap<TreeReference, QuickTriggerable> conditionRepeatTargetIndex; // <TreeReference,
                                                                                // Condition>;

   public LegacyDagImpl() {
      triggerablesDAG = new ArrayList<QuickTriggerable>();
      triggerIndex = new HashMap<TreeReference, HashSet<QuickTriggerable>>();
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
	public void deleteRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, 
			TreeReference deleteRef, TreeElement parentElement, TreeElement deletedElement) {
		
		triggerTriggerables(mainInstance, evalContext, deleteRef, true);
	}
	
	@Override
	public void createRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, 
			TreeReference createRef, TreeElement parentElement, TreeElement createdElement) {

		// trigger conditions that depend on the creation of this new node
		triggerTriggerables(mainInstance, evalContext, createRef, true);
      // initialize conditions for the node (and sub-nodes)
		initializeTriggerables(mainInstance, evalContext, createRef, true);
	}
	
	@Override
	public void copyItemsetAnswer(FormInstance mainInstance, EvaluationContext evalContext, 
			TreeReference copyRef, TreeElement copyToElement, boolean cascadeToGroupChildren) {

		// trigger conditions that depend on the creation of this new node
	      triggerTriggerables(mainInstance, evalContext, copyRef, cascadeToGroupChildren);
	      // initialize conditions for the node (and sub-nodes)
	      initializeTriggerables(mainInstance, evalContext, copyRef, cascadeToGroupChildren);
	      // not 100% sure this will work since destRef is ambiguous as the last
	      // step, but i think it's supposed to work
	}

	/**
    * Finalize the DAG associated with the form's triggered conditions. This
    * will create the appropriate ordering and dependencies to ensure the
    * conditions will be evaluated in the appropriate orders.
    *
    * @throws IllegalStateException
    *            - If the trigger ordering contains an illegal cycle and the
    *            triggers can't be laid out appropriately
    */
   @Override
   public void finalizeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, ArrayList<QuickTriggerable> unorderedTriggereables, HashMap<TreeReference, HashSet<QuickTriggerable>> triggerIndex) throws IllegalStateException {
	  this.triggerIndex = triggerIndex;
      //
      // DAGify the triggerables based on dependencies and sort them so that
      // triggerables come only after the triggerables they depend on
      //

      ArrayList<QuickTriggerable> vertices = new ArrayList<QuickTriggerable>(unorderedTriggereables);
      triggerablesDAG.clear();
      ArrayList<QuickTriggerable[]> partialOrdering = new ArrayList<QuickTriggerable[]>();
      HashSet<QuickTriggerable> deps = new HashSet<QuickTriggerable>();
      HashSet<QuickTriggerable> newDestinationSet = new HashSet<QuickTriggerable>();
      for (QuickTriggerable qt : vertices) {
         deps.clear();
         newDestinationSet.clear();
         fillTriggeredElements(mainInstance, evalContext, qt, deps, newDestinationSet, true, true);

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
      HashSet<QuickTriggerable> roots = new HashSet<QuickTriggerable>(vertices.size());
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
               message += "\nThe following nodes are likely involved in the loop:" + hints;
            }
            throw new IllegalStateException(message);
         }

         // order the root nodes - mainly for clean textual diffs when printing
         // When this code is rewritten to use HashSet, this will become
         // important.
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

      conditionRepeatTargetIndex = new HashMap<TreeReference, QuickTriggerable>();
      for (int i = 0; i < triggerablesDAG.size(); i++) {
         QuickTriggerable qt = triggerablesDAG.get(i);
         if (qt.t instanceof Condition) {
            ArrayList<TreeReference> targets = qt.t.getTargets();
            for (int j = 0; j < targets.size(); j++) {
               TreeReference target = (TreeReference) targets.get(j);
               if (mainInstance.getTemplate(target) != null) {
                  conditionRepeatTargetIndex.put(target, qt);
               }
            }
         }
      }

//      printTriggerables();
   }

   /**
    * Get all of the elements which will need to be evaluated (in order) when
    * the triggerable is fired.
    * 
    * @param t
    * @param destination
    *           where to store the triggerables
    * @param cascadeToChildrenOfGroupsWithRelevanceExpressions
    *           if true, then the slow code will execute, if false, then
    *           old/fast code will execute that suffers from
    *           https://code.google.com/p/opendatakit/issues/detail?id=888
    */
   public void fillTriggeredElements(FormInstance mainInstance, EvaluationContext evalContext, QuickTriggerable qt, Set<QuickTriggerable> destinationSet,
         Set<QuickTriggerable> newDestinationSet, boolean expandRepeatables,
         boolean cascadeToChildrenOfGroupsWithRelevanceExpressions) {
      if (qt.t.canCascade()) {
         if (mode == EvalBehavior.Legacy) {
            for (int j = 0; j < qt.t.getTargets().size(); j++) {
               TreeReference target = qt.t.getTargets().get(j);
               HashSet<QuickTriggerable> triggered = triggerIndex.get(target);
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
            for (int j = 0; j < qt.t.getTargets().size(); j++) {
               TreeReference target = (TreeReference) qt.t.getTargets().get(j);
               Set<TreeReference> updatedNodes = new HashSet<TreeReference>();
               updatedNodes.add(target);

               // For certain types of triggerables, the update will affect not
               // only
               // the target, but
               // also the children of the target. In that case, we want to add
               // all
               // of those nodes
               // to the list of updated elements as well.
               if (qt.t.isCascadingToChildren()) {
                  addChildrenOfReference(mainInstance, evalContext, target, updatedNodes, expandRepeatables);
               }

               // Now go through each of these updated nodes (generally just 1
               // for a
               // normal calculation,
               // multiple nodes if there's a relevance cascade.
               for (TreeReference ref : updatedNodes) {
                  // Check our index to see if that target is a Trigger for
                  // other
                  // conditions
                  // IE: if they are an element of a different calculation or
                  // relevancy calc

                  // We can't make this reference generic before now or we'll
                  // lose the
                  // target information,
                  // so we'll be more inclusive than needed and see if any of
                  // our
                  // triggers are keyed on
                  // the predicate-less path of this ref
                  HashSet<QuickTriggerable> triggered = triggerIndex.get(ref.hasPredicates() ? ref
                        .removePredicates() : ref);

                  if (triggered != null) {
                     // If so, walk all of these triggerables that we found
                     for (QuickTriggerable qu : triggered) {
                        // And add them to the queue if they aren't there
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
    * Walks the current set of conditions, and evaluates each of them with the
    * current context.
    */
   
   @Override
   public void initializeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, 
			TreeReference rootRef, boolean cascadeToGroupChildren) {
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

      evaluateTriggerables(mainInstance, evalContext, applicable, rootRef, cascadeToGroupChildren);
   }

   /**
    * The entry point for the DAG cascade after a value is changed in the model.
    *
    * @param ref
    *           The full contextualized unambiguous reference of the value that
    *           was changed.
    */
   @Override
   public void triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext, TreeReference ref,
         boolean cascadeToChildrenOfGroupsWithRelevanceExpressions) {

      // turn unambiguous ref into a generic ref
      // to identify what nodes should be triggered by this
      // reference changing
      TreeReference genericRef = ref.genericize();

      // get triggerables which are activated by the generic reference
      HashSet<QuickTriggerable> triggered = triggerIndex.get(genericRef);
      if (triggered == null) {
         return;
      }

      Set<QuickTriggerable> triggeredCopy = new HashSet<QuickTriggerable>(triggered);

      // Evaluate all of the triggerables in our new vector
      evaluateTriggerables(mainInstance, evalContext, triggeredCopy, ref, cascadeToChildrenOfGroupsWithRelevanceExpressions);
   }

   /**
    * Step 2 in evaluating DAG computation updates from a value being changed in
    * the instance. This step is responsible for taking the root set of directly
    * triggered conditions, identifying which conditions should further be
    * triggered due to their update, and then dispatching all of the
    * evaluations.
    *
    * @param tv
    *           A vector of all of the trigerrables directly triggered by the
    *           value changed
    * @param anchorRef
    *           The reference to original value that was updated
    */
   private void evaluateTriggerables(FormInstance mainInstance, EvaluationContext evalContext, Set<QuickTriggerable> tv, TreeReference anchorRef,
         boolean cascadeToChildrenOfGroupsWithRelevanceExpressions) {
      // add all cascaded triggerables to queue

      // Iterate through all of the currently known triggerables to be triggered
      Set<QuickTriggerable> refSet = new HashSet<QuickTriggerable>(tv);
      for (; !refSet.isEmpty();) {
         Set<QuickTriggerable> newSet = new HashSet<QuickTriggerable>();
         for (QuickTriggerable qt : refSet) {
            if (mode == EvalBehavior.Legacy || mode == EvalBehavior.April_2014) {
               fillTriggeredElements(mainInstance, evalContext, qt, tv, newSet, false,
                     cascadeToChildrenOfGroupsWithRelevanceExpressions);
            } else if (mode == EvalBehavior.Fast_2014) {
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
         }
         refSet = newSet;
      }

      // tv should now contain all of the triggerable components which are going
      // to need to be addressed
      // by this update.
      // 'triggerables' is topologically-ordered by dependencies, so evaluate
      // the triggerables in 'tv'
      // in the order they appear in 'triggerables'
      for (int i = 0; i < triggerablesDAG.size(); i++) {
         QuickTriggerable qt = triggerablesDAG.get(i);
         if (tv.contains(qt)) {
            evaluateTriggerable(mainInstance, evalContext, qt, anchorRef);
         }
      }
   }

   /**
    * Step 3 in DAG cascade. evaluate the individual triggerable expressions
    * against the anchor (the value that changed which triggered recomputation)
    *
    * @param t
    *           The triggerable to be updated
    * @param anchorRef
    *           The reference to the value which was changed.
    */
   private void evaluateTriggerable(FormInstance mainInstance, EvaluationContext evalContext, QuickTriggerable qt, TreeReference anchorRef) {

      // Contextualize the reference used by the triggerable against the anchor
      TreeReference contextRef = qt.t.contextualizeContextRef(anchorRef);
      try {

         // Now identify all of the fully qualified nodes which this triggerable
         // updates. (Multiple nodes can be updated by the same trigger)
         List<TreeReference> v = evalContext.expandReference(contextRef);

         // Go through each one and evaluate the trigger expresion
         for (int i = 0; i < v.size(); i++) {
            EvaluationContext ec = new EvaluationContext(evalContext, v.get(i));
            qt.t.apply(mainInstance, ec, v.get(i));
         }
      } catch (Exception e) {
         throw new WrappedException("Error evaluating field '" + contextRef.getNameLast() + "': "
               + e.getMessage(), e);
      }
   }

   /**
    * This is a utility method to get all of the references of a node. It can be
    * replaced when we support dependent XPath Steps (IE: /path/to//)
    */
   public void addChildrenOfReference(FormInstance mainInstance, EvaluationContext evalContext, TreeReference original, Set<TreeReference> toAdd,
         boolean expandRepeatables) {
      // original has already been added to the 'toAdd' list.

      TreeElement repeatTemplate = expandRepeatables ? mainInstance.getTemplatePath(original)
            : null;
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
   private void addChildrenOfElement(FormInstance mainInstance, EvaluationContext evalContext, TreeElement el, Set<TreeReference> toAdd,
         boolean expandRepeatables) {
      TreeElement repeatTemplate = expandRepeatables ? mainInstance.getTemplatePath(el.getRef())
            : null;
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
}
