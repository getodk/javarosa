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
import java.util.HashMap;
import java.util.HashSet;

import org.javarosa.core.model.FormDef.EvalBehavior;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;

/**
 * The fast eval logic for 2014
 *
 * @author mitchellsundt@gmail.com
 * @author meletis@surveycto.com
 *
 */
public class Fast2014DagImpl extends IDag {

	@Override
	public EvalBehavior getEvalBehavior() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QuickTriggerable getTriggerableForRepeatGroup(TreeReference ref) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void triggerTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			boolean cascadeToGroupChildren) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteRepeatGroup(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement parentElement, TreeElement deletedElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createRepeatGroup(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement parentElement, TreeElement createdElement) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void copyItemsetAnswer(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference ref,
			TreeElement copyToElement, boolean cascadeToGroupChildren) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finalizeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext,
			ArrayList<QuickTriggerable> unorderedTriggereables,
			HashMap<TreeReference, HashSet<QuickTriggerable>> triggerIndex)
			throws IllegalStateException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initializeTriggerables(FormInstance mainInstance,
			EvaluationContext evalContext, TreeReference rootRef,
			boolean cascadeToGroupChildren) {
		// TODO Auto-generated method stub
		
	}
}
