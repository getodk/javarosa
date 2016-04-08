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
import java.util.List;

import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

/**
 * The April 2014 eval logic as of javarosa-2014-04-29.jar (also applies to
 * javarosa-2014-03-07.jar and later).
 *
 * @author mitchellsundt@gmail.com
 * @author meletis@surveycto.com
 *
 */
public class April2014DagImpl extends IDag {

   private final EvalBehavior mode = EvalBehavior.April_2014;

   public April2014DagImpl(EventNotifierAccessor accessor) {
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
         TreeReference copyRef, TreeElement copyToElement, boolean midSurvey) {

      // trigger conditions that depend on the creation of this new node
      triggerTriggerables(mainInstance, evalContext, copyRef, midSurvey);
      // initialize conditions for the node (and sub-nodes)
      initializeTriggerables(mainInstance, evalContext, copyRef, midSurvey);
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
   public void finalizeTriggerables(FormInstance mainInstance, EvaluationContext evalContext)
         throws IllegalStateException {
      //
      // DAGify the triggerables based on dependencies and sort them so that
      // triggerables come only after the triggerables they depend on
      //

      triggerablesDAG.clear();

      ArrayList<QuickTriggerable[]> partialOrdering = new ArrayList<QuickTriggerable[]>();
      for (int i = 0; i < unorderedTriggerables.size(); i++) {
         QuickTriggerable qt = unorderedTriggerables.get(i);

         ArrayList<QuickTriggerable> deps = new ArrayList<QuickTriggerable>();
         fillTriggeredElements(evalContext, qt, deps);

         for (int j = 0; j < deps.size(); j++) {
            QuickTriggerable qu = deps.get(j);
            QuickTriggerable[] edge = { qt, qu };
            partialOrdering.add(edge);
         }
      }

      ArrayList<QuickTriggerable> vertices = new ArrayList<QuickTriggerable>(unorderedTriggerables);
      ArrayList<QuickTriggerable> roots = new ArrayList<QuickTriggerable>(
            unorderedTriggerables.size());
      while (vertices.size() > 0) {
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

         // remove root nodes and edges originating from them
         for (int i = 0; i < roots.size(); i++) {
            QuickTriggerable root = roots.get(i);
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
    */
   private void fillTriggeredElements(EvaluationContext evalContext, QuickTriggerable qt,
         ArrayList<QuickTriggerable> destination) {
      if (qt.t.canCascade()) {
         for (int j = 0; j < qt.t.getTargets().size(); j++) {
            TreeReference target = qt.t.getTargets().get(j);
            ArrayList<TreeReference> updatedNodes = new ArrayList<TreeReference>();
            updatedNodes.add(target);

            // For certain types of triggerables, the update will affect not
            // only the target, but also the children of the target. In that 
            // case, we want to add all of those nodes to the list of updated
            // elements as well.
            if (qt.t.isCascadingToChildren()) {
               addChildrenOfReference(evalContext, target, updatedNodes);
            }

            // Now go through each of these updated nodes (generally just 1
            // for a normal calculation, multiple nodes if there's a relevance
            // cascade.
            for (TreeReference ref : updatedNodes) {
               // Check our index to see if that target is a Trigger for
               // other conditions
               // IE: if they are an element of a different calculation or
               // relevancy calc

               // We can't make this reference generic before now or we'll
               // lose the target information, so we'll be more inclusive 
               // than needed and see if any of our triggers are keyed on
               // the predicate-less path of this ref
               ArrayList<QuickTriggerable> triggered = triggerIndex.get(ref.hasPredicates() ? ref
                     .removePredicates() : ref);

               if (triggered != null) {
                  // If so, walk all of these triggerables that we found
                  for (int k = 0; k < triggered.size(); k++) {
                     QuickTriggerable qu = triggered.get(k);

                     // And add them to the queue if they aren't there
                     // already
                     if (!destination.contains(qu))
                        destination.add(qu);
                  }
               }
            }

         }
      }
   }

   /**
    * This is a utility method to get all of the references of a node. It can be
    * replaced when we support dependent XPath Steps (IE: /path/to//)
    */
   private void addChildrenOfReference(EvaluationContext evalContext, TreeReference original,
         ArrayList<TreeReference> toAdd) {
      for (TreeReference ref : evalContext.expandReference(original)) {
         addChildrenOfElement(evalContext.resolveReference(ref), toAdd);
      }
   }

   // Recursive step of utility method
   private void addChildrenOfElement(TreeElement el, ArrayList<TreeReference> toAdd) {
      for (int i = 0; i < el.getNumChildren(); ++i) {
         TreeElement child = el.getChildAt(i);
         toAdd.add(child.getRef().genericize());
         addChildrenOfElement(child, toAdd);
      }
   }

   /**
    * Walks the current set of conditions, and evaluates each of them with the
    * current context.
    */

   @Override
   public Collection<QuickTriggerable> initializeTriggerables(FormInstance mainInstance, EvaluationContext evalContext,
         TreeReference rootRef, boolean midSurvey) {
      TreeReference genericRoot = rootRef.genericize();

      ArrayList<QuickTriggerable> applicable = new ArrayList<QuickTriggerable>();
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

      return evaluateTriggerables(mainInstance, evalContext, applicable, rootRef);
   }

   /**
    * The entry point for the DAG cascade after a value is changed in the model.
    *
    * @param ref
    *           The full contextualized unambiguous reference of the value that
    *           was changed.
    */
   @Override
   public Collection<QuickTriggerable> triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext,
         TreeReference ref, boolean midSurvey) {
      // turn unambiguous ref into a generic ref
      // to identify what nodes should be triggered by this
      // reference changing
      TreeReference genericRef = ref.genericize();

      // get triggerables which are activated by the generic reference
      ArrayList<QuickTriggerable> triggered = triggerIndex.get(genericRef);
      if (triggered == null) {
         return Collections.emptySet();
      }

      ArrayList<QuickTriggerable> triggeredCopy = new ArrayList<QuickTriggerable>(triggered);

      // Evaluate all of the triggerables in our new list
      return evaluateTriggerables(mainInstance, evalContext, triggeredCopy, ref);
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
   private Collection<QuickTriggerable> evaluateTriggerables(FormInstance mainInstance, EvaluationContext evalContext,
         ArrayList<QuickTriggerable> tv, TreeReference anchorRef) {
      // add all cascaded triggerables to queue

      // Iterate through all of the currently known triggerables to be
      // triggered
      for (int i = 0; i < tv.size(); i++) {
         QuickTriggerable qt = tv.get(i);
         fillTriggeredElements(evalContext, qt, tv);
      }

      // tv should now contain all of the triggerable components which are
      // going to need to be addressed by this update.
      // 'triggerables' is topologically-ordered by dependencies, so evaluate
      // the triggerables in 'tv' in the order they appear in 'triggerables'
      for (int i = 0; i < triggerablesDAG.size(); i++) {
         QuickTriggerable qt = triggerablesDAG.get(i);
         if (tv.contains(qt)) {
            evaluateTriggerable(mainInstance, evalContext, qt, anchorRef);
         }
      }

      return tv;
   }

   /**
    * Step 3 in DAG cascade. evaluate the individual triggerable expressions
    * against the anchor (the value that changed which triggered recomputation)
    *
    * @param qt
    *           The triggerable to be updated
    * @param anchorRef
    *           The reference to the value which was changed.
    */
   private void evaluateTriggerable(FormInstance mainInstance, EvaluationContext evalContext,
         QuickTriggerable qt, TreeReference anchorRef) {

      // Contextualize the reference used by the triggerable against the
      // anchor
      TreeReference contextRef = qt.t.contextualizeContextRef(anchorRef);
      try {

         // Now identify all of the fully qualified nodes which this
         // triggerable updates. (Multiple nodes can be updated by the
         // same trigger)
         List<TreeReference> v = evalContext.expandReference(contextRef);

         // Go through each one and evaluate the trigger expresion
         for (int i = 0; i < v.size(); i++) {
            EvaluationContext ec = new EvaluationContext(evalContext, v.get(i));
            qt.t.apply(mainInstance, ec, v.get(i));
         }
      } catch (Exception e) {
         throw new RuntimeException("Error evaluating field '" + contextRef.getNameLast() + "': "
               + e.getMessage(), e);
      }
   }

   @Override
   public boolean shouldTrustPreviouslyCommittedAnswer() {
      return false;
   }
}
