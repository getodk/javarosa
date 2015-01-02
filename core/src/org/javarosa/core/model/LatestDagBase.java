/*
 * Copyright (C) 2014 University of Washington
 *
 * Originally developed by Dobility, Inc. (as part of SurveyCTO)
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

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.debug.Event;

import java.util.*;

/**
 * @author Meletis Margaritis
 */
public abstract class LatestDagBase extends IDag {

   protected LatestDagBase(EventNotifierAccessor accessor) {
      super(accessor);
   }

   protected Set<QuickTriggerable> doEvaluateTriggerables(FormInstance mainInstance, EvaluationContext evalContext, Set<QuickTriggerable> tv, TreeReference anchorRef, Set<QuickTriggerable> alreadyEvaluated) {
      // tv should now contain all of the triggerable components which are
      // going
      // to need to be addressed
      // by this update.
      // 'triggerables' is topologically-ordered by dependencies, so evaluate
      // the triggerables in 'tv'
      // in the order they appear in 'triggerables'
      Set<QuickTriggerable> fired = new HashSet<QuickTriggerable>();

      Map<TreeReference, List<TreeReference>> firedAnchors = new LinkedHashMap<TreeReference, List<TreeReference>>();

      for (QuickTriggerable qt : triggerablesDAG) {
         if (tv.contains(qt) && !alreadyEvaluated.contains(qt)) {

            List<TreeReference> affectedTriggers = qt.t.findAffectedTriggers(firedAnchors);
            if (affectedTriggers.isEmpty()) {
               affectedTriggers.add(anchorRef);
            }

            List<EvaluationResult> evaluationResults = evaluateTriggerable(
                    mainInstance, evalContext, qt, affectedTriggers);

            if (evaluationResults.size() > 0) {
               fired.add(qt);

               for (EvaluationResult evaluationResult : evaluationResults) {
                  TreeReference affectedRef = evaluationResult.getAffectedRef();

                  TreeReference key = affectedRef.genericize();
                  List<TreeReference> values = firedAnchors.get(key);
                  if (values == null) {
                     values = new ArrayList<TreeReference>();
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
    *
    * @param qt
    *            The triggerable to be updated
    * @param anchorRefs
    */
   private List<EvaluationResult> evaluateTriggerable(FormInstance mainInstance,
                                                      EvaluationContext evalContext, QuickTriggerable qt,
                                                      List<TreeReference> anchorRefs) {

      List<EvaluationResult> evaluationResults = new ArrayList<EvaluationResult>(0);

      // Contextualize the reference used by the triggerable against the
      // anchor
      Set<TreeReference> updatedContextRef = new HashSet<TreeReference>();

      for (TreeReference anchorRef : anchorRefs) {
         TreeReference contextRef = qt.t.contextualizeContextRef(anchorRef);
         if (updatedContextRef.contains(contextRef)) {
            continue;
         }

         try {

            // Now identify all of the fully qualified nodes which this
            // triggerable
            // updates. (Multiple nodes can be updated by the same trigger)
            List<TreeReference> qualifiedList = evalContext
                    .expandReference(contextRef);

            // Go through each one and evaluate the trigger expression
            for (TreeReference qualified : qualifiedList) {
               EvaluationContext ec = new EvaluationContext(evalContext,
                       qualified);
               evaluationResults.addAll(qt.t.apply(mainInstance, ec,
                       qualified));
            }

            boolean fired = evaluationResults.size() > 0;
            if (fired) {
               accessor.getEventNotifier().publishEvent(
                       new Event(qt.t.getClass().getSimpleName(),
                               evaluationResults));
            }

            updatedContextRef.add(contextRef);
         } catch (Exception e) {
            throw new RuntimeException("Error evaluating field '"
                    + contextRef.getNameLast() + "': " + e.getMessage(), e);
         }
      }

      return evaluationResults;
   }
}
