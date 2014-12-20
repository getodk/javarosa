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
import java.util.HashSet;
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
	
	// <TreeReference, Condition>;
	// associates repeatable nodes with the Condition that determines their relevancy
	protected HashMap<TreeReference, QuickTriggerable> conditionRepeatTargetIndex;
	
	// this list is topologically ordered, meaning for any tA and tB in
	// the list, where tA comes before tB, evaluating tA cannot depend on any
	// result from evaluating tB
	protected ArrayList<QuickTriggerable> triggerablesDAG;

	// Maps a tree reference to the set of triggerables that need to be
	// processed when the value at this reference changes.
	protected HashMap<TreeReference, HashSet<QuickTriggerable>> triggerIndex;
	
	/**
	 * The EvalBehavior that the implementation provides.
	 * 
	 * @return
	 */
	public abstract EvalBehavior getEvalBehavior();

	public abstract QuickTriggerable getTriggerableForRepeatGroup(TreeReference ref);

	public abstract void triggerTriggerables(FormInstance mainInstance, EvaluationContext evalContext, 
			TreeReference ref, boolean cascadeToGroupChildren);

	public abstract void deleteRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, TreeReference ref,
			TreeElement parentElement, TreeElement deletedElement);

	public abstract void createRepeatGroup(FormInstance mainInstance, EvaluationContext evalContext, TreeReference ref,
			TreeElement parentElement, TreeElement createdElement);

	public abstract void copyItemsetAnswer(FormInstance mainInstance, EvaluationContext evalContext, TreeReference ref,
			TreeElement copyToElement, boolean cascadeToGroupChildren);

	public abstract void finalizeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, 
			ArrayList<QuickTriggerable> unorderedTriggereables, HashMap<TreeReference, HashSet<QuickTriggerable>> triggerIndex) throws IllegalStateException;

	public abstract void initializeTriggerables(FormInstance mainInstance, EvaluationContext evalContext, 
			TreeReference rootRef, boolean cascadeToGroupChildren);

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
	public IConditionExpr getConditionExpressionForTrueAction(FormInstance mainInstance,
			TreeElement instanceNode, int action) {
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
