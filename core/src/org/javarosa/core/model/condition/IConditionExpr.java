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

package org.javarosa.core.model.condition;

import java.util.Set;
import java.util.List;

import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * A condition expression is an expression which is evaluated against the current
 * model and produces a value. These objects should keep track of the expression that
 * they evaluate, as well as being able to identify what references will require the
 * condition to be triggered.
 *
 * As a metaphor into an XForm, a Condition expression represents an XPath expression
 * which you can query for a value (in a calculate or relevancy condition, for instance),
 * can tell you what nodes its value will depend on, and optionally what values it "Pivots"
 * around. (IE: if an expression's value is true if a node is > 25, and false otherwise, it
 * has a "Comparison Pivot" around 25).
 *
 * @author ctsims
 *
 */
public interface IConditionExpr extends Externalizable {

	/**
	 * Evaluate this expression against the current models and
	 * context and provide a true or false value.
	 *
	 * @param model
	 * @param evalContext
	 * @return
	 */
	boolean eval (FormInstance model, EvaluationContext evalContext);

	/**
	 * Evaluate this expression against the current models and
	 * context and provide the final value of the expression, without
	 * forcing a cast to a boolean value.
	 *
	 * @param model
	 * @param evalContext
	 * @return
	 */
	Object evalRaw (FormInstance model, EvaluationContext evalContext);

	/**
	 * Used for itemsets. Fill this documentation in.
	 *
	 * @param model
	 * @param evalContext
	 * @return
	 */
	String evalReadable (FormInstance model, EvaluationContext evalContext);

	/**
	 * Used for itemsets. Fill this documentation in.
	 *
	 * @param model
	 * @param evalContext
	 * @return
	 */
   List<TreeReference> evalNodeset (FormInstance model, EvaluationContext evalContext);

	/**
	 * Provides a list of all of the references that this expression's value depends upon
	 * directly. These values can't be contextualized fully (since these triggers are necessary
	 * before runtime), but should only need to be contextualized to be a complete set.
	 *
	 * @return
	 */
	Set<TreeReference> getTriggers (TreeReference contextRef); /* unordered set of TreeReference */

	/**
	 * Provide a list of Pivots around which this Condition Expression depends.
	 *
	 * Optional to implement. If not implemented, throw an Unpivotable Expression exception
	 * to signal that the expression cannot be statically evaluated.
	 *
	 * @param model
	 * @param evalContext
	 * @return
	 * @throws UnpivotableExpressionException
	 */
   List<Object> pivot(FormInstance model, EvaluationContext evalContext) throws UnpivotableExpressionException;
}
