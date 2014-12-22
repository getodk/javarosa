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
import java.util.HashMap;
import java.util.Set;

import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XPathReference;

/**
 * Abstract interface of the DAG management and triggerable processing logic
 *
 * @author mitchellsundt@gmail.com
 *
 */
public abstract class IDag {

	// this list is topologically ordered, meaning for any tA and tB in
	// the list, where tA comes before tB, evaluating tA cannot depend on any
	// result from evaluating tB
	protected ArrayList<QuickTriggerable> triggerablesDAG;

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
	 * @param cascadeToGroupChildren
	 */
	public abstract void triggerTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			boolean cascadeToGroupChildren);

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
	 * @param cascadeToGroupChildren
	 */
	public abstract void copyItemsetAnswer(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement copyToElement, boolean cascadeToGroupChildren);

	/**
	 * Initialize the triggerableDAG array and the triggerIndex array.
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param unorderedTriggereables
	 * @param triggerIndex
	 * @throws IllegalStateException
	 */
	public abstract void finalizeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext,
			ArrayList<QuickTriggerable> unorderedTriggereables,
			HashMap<TreeReference, ArrayList<QuickTriggerable>> triggerIndex)
			throws IllegalStateException;

	/**
	 * Invoked externally when a new mainInstance is loaded (initial sweep of
	 * calculates). Invoked internally when creating repeat groups and copying
	 * itemsets(?).
	 * 
	 * @param mainInstance
	 * @param evalContext
	 * @param rootRef
	 * @param cascadeToGroupChildren
	 */
	public abstract void initializeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference rootRef,
			boolean cascadeToGroupChildren);

	protected void publishSummary(String lead,
			Set<QuickTriggerable> quickTriggerables) {
		System.out.println(lead + ": " + quickTriggerables.size()
				+ " triggerables were fired.");
	}

	/**
	 * For debugging - note that path assumes Android device
	 */
	public void printTriggerables() {
		OutputStreamWriter w = null;
		try {
			w = new OutputStreamWriter(new FileOutputStream(new File(
					"/sdcard/odk/trigger.log")), "UTF-8");
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
	public IConditionExpr getConditionExpressionForTrueAction(
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
					ArrayList<TreeReference> targets = c.getTargets();
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
}
