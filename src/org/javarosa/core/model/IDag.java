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
import java.util.*;

import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.condition.Triggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.debug.Event;
import org.javarosa.debug.EventNotifier;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParserReporter;

/**
 * Abstract interface of the DAG management and triggerable processing logic.
 * 
 * Some method bodies are intentionally duplicated in the derived classes
 * so as to ensure that future changes do not affect the existing implementations.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public abstract class IDag {

	public interface EventNotifierAccessor {
		public EventNotifier getEventNotifier();
	}
	
	protected final EventNotifierAccessor accessor;
	
   // NOT VALID UNTIL finalizeTriggerables() is called!!
   //
   // Topologically ordered list, meaning that for any tA and tB in
	// the list, where tA comes before tB, evaluating tA cannot depend on any
	// result from evaluating tB. 
	protected final ArrayList<QuickTriggerable> triggerablesDAG
	   = new ArrayList<QuickTriggerable>();

	// NOT VALID UNTIL finalizeTriggerables() is called!!
   // 
   // Associates repeatable nodes with the Condition that determines their
   // relevance.
	protected final HashMap<TreeReference, QuickTriggerable> conditionRepeatTargetIndex
      = new HashMap<TreeReference, QuickTriggerable>();

   // Maps a tree reference to the set of triggerables that need to be
   // processed when the value at this reference changes.
	protected final HashMap<TreeReference, ArrayList<QuickTriggerable>> triggerIndex 
      = new HashMap<TreeReference, ArrayList<QuickTriggerable>>();

   // List of all the triggerables in the form. Unordered.
	protected final ArrayList<QuickTriggerable> unorderedTriggerables
      = new ArrayList<QuickTriggerable>();

	protected IDag(EventNotifierAccessor accessor) {
		this.accessor = accessor;
	}
	
	/**
	 * The EvalBehavior that the implementation provides.
	 * 
	 * @return
	 */
	public abstract EvalBehavior getEvalBehavior();

	/**
	 * Used to obtain the triggerable that impacts the relevancy of the repeat
	 * group.
	 * 
	 * @param ref
	 * @return
	 */
	public abstract QuickTriggerable getTriggerableForRepeatGroup(
			TreeReference ref);

	/**
	 * Fire a triggereable.
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param ref
	 * @param midSurvey
	 */
	public abstract Collection<QuickTriggerable> triggerTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			boolean midSurvey);

	/**
	 * Take whatever action is required when deleting a repeat group.
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param ref
	 * @param parentElement
	 * @param deletedElement
	 */
	public abstract void deleteRepeatGroup(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement parentElement, TreeElement deletedElement);

	/**
	 * Take whatever action is required when creating a repeat group.
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param ref
	 * @param parentElement
	 * @param createdElement
	 */
	public abstract void createRepeatGroup(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement parentElement, TreeElement createdElement);

	/**
	 * Take actions related to changes of select-one and select-multiple
	 * itemsets.
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param ref
	 * @param copyToElement
	 * @param midSurvey
	 */
	public abstract void copyItemsetAnswer(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement copyToElement, boolean midSurvey);

	/**
	 * Add the triggerables to the dataset prior to finalizing.
	 * 
	 * @param t triggerable to add
	 */

   private final QuickTriggerable findTriggerable(Triggerable t) {
      for (QuickTriggerable qt : unorderedTriggerables) {
         if (t.equals(qt.t)) {
            return qt;
         }
      }
      return null;
   }

   /**
    * Add the triggerables to the dataset prior to finalizing.
    * 
    * @param t
    */
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
            ArrayList<QuickTriggerable> triggered = triggerIndex.get(trigger);
            if (triggered == null) {
               triggered = new ArrayList<QuickTriggerable>();
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
	 * Initialize the triggerableDAG array and the triggerIndex array.
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @throws IllegalStateException
	 */
	public abstract void finalizeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext)
			throws IllegalStateException;

	/**
	 * Invoked externally when a new mainInstance is loaded (initial sweep of
	 * calculates). Invoked internally when creating repeat groups and copying
	 * itemsets(?).
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param rootRef
	 * @param midSurvey true if we are during the survey, false if we are on loading/saving phase
	 */
	public abstract Collection<QuickTriggerable> initializeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference rootRef,
			boolean midSurvey);
	
	/**
	 * Invoked to validate a filled-in form. Sweeps through from beginning
	 * to end, confirming that the entered values satisfy all constraints.
	 * The FormEntryController is based upon the FormDef, but has its own
	 * model and controller independent of anything at the UI layer.
	 * 
	 * @param formEntryControllerToBeValidated
	 * @param markCompleted
	 * @return
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

            int saveStatus =
                    formEntryControllerToBeValidated.answerQuestion(formControllerToBeValidatedFormIndex,
                            formEntryControllerToBeValidated.getModel().getQuestionPrompt().getAnswerValue(), false);
            if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) {
               // jump to the error
               ValidateOutcome vo = new ValidateOutcome(formControllerToBeValidatedFormIndex,
                       saveStatus);
               return vo;
            }
         }
      }
      return null;
   }

   /**
    * Specifies if unchanged answers should be trusted (and not re-committed).
    *
    * @return
    */
   public abstract boolean shouldTrustPreviouslyCommittedAnswer();

   protected void publishSummary(String lead, Collection<QuickTriggerable> quickTriggerables) {
      publishSummary(lead, null, quickTriggerables);
   }

   protected final void publishSummary(String lead,
                                 TreeReference ref,
                                 Collection<QuickTriggerable> quickTriggerables) {
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
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (w != null) {
				try {
					w.flush();
					w.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Pull this in from FormOverview so that we can make fields private.
	 * 
	 * @param instanceNode
	 * @param action
	 * @return
	 */
	public final IConditionExpr getConditionExpressionForTrueAction(
			FormInstance mainInstance, TreeElement instanceNode, int action) {
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

						TreeReference tr = (TreeReference) (new XPathReference(
								target)).getReference();
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
	 * @return
	 */  
   public final ArrayList<Condition> getConditions() {
      ArrayList<Condition> conditions = new ArrayList<Condition>();
      for ( QuickTriggerable qt : unorderedTriggerables ) {
         if ( qt.t instanceof Condition ) {
            conditions.add((Condition) qt.t);
         }
      }
      return conditions;
   }
   
	/**
	 * API for retrieving thelist of recalculates, for use when
	 * serializing the form definition (e.g., into .cache file).
	 * @return
	 */
   public final ArrayList<Recalculate> getRecalculates() {
      ArrayList<Recalculate> recalculates = new ArrayList<Recalculate>();
      for ( QuickTriggerable qt : unorderedTriggerables ) {
         if ( qt.t instanceof Recalculate ) {
            recalculates.add((Recalculate) qt.t);
         }
      }
      return recalculates;
   }

   public final void reportDependencyCycles (XFormParserReporter reporter) {
      HashSet<TreeReference> vertices = new HashSet<TreeReference>();
      ArrayList<TreeReference[]> edges = new ArrayList<TreeReference[]>();

      //build graph
      ArrayList<TreeReference> targets = new ArrayList<TreeReference>();
      for (TreeReference trigger : triggerIndex.keySet()) {
         if (!vertices.contains(trigger))
            vertices.add(trigger);
         ArrayList<QuickTriggerable> triggered = triggerIndex.get(trigger);
         targets.clear();
         for (QuickTriggerable qt : triggered ) {
            Triggerable t = qt.t;
            for (int j = 0; j < t.getTargets().size(); j++) {
               TreeReference target = t.getTargets().get(j);
               if (!targets.contains(target))
                  targets.add(target);
            }
         }

         for (int i = 0; i < targets.size(); i++) {
            TreeReference target = targets.get(i);
            if (!vertices.contains(target))
               vertices.add(target);

            TreeReference[] edge = {trigger, target};
            edges.add(edge);
         }
      }

      //find cycles
      boolean acyclic = true;
      HashSet<TreeReference> leaves = new HashSet<TreeReference>(vertices.size());
      while (vertices.size() > 0) {
         //determine leaf nodes
         leaves.clear();
         leaves.addAll(vertices);
         for (int i = 0; i < edges.size(); i++) {
            TreeReference[] edge = (TreeReference[])edges.get(i);
            leaves.remove(edge[0]);
         }

         //if no leaf nodes while graph still has nodes, graph has cycles
         if (leaves.size() == 0) {
            acyclic = false;
            break;
         }

         //remove leaf nodes and edges pointing to them
         for (TreeReference leaf : leaves ) {
            vertices.remove(leaf);
         }
         for (int i = edges.size() - 1; i >= 0; i--) {
            TreeReference[] edge = (TreeReference[])edges.get(i);
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
         reporter.error(b.toString());

         throw new RuntimeException("Dependency cycles amongst the xpath expressions in relevant/calculate");
      }
   }

}
