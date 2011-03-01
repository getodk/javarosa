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

import java.util.Vector;

import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.util.externalizable.Externalizable;

public interface IConditionExpr extends Externalizable {
	boolean eval (FormInstance model, EvaluationContext evalContext);
	Object evalRaw (FormInstance model, EvaluationContext evalContext);
	String evalReadable (FormInstance model, EvaluationContext evalContext);
	Vector evalNodeset (FormInstance model, EvaluationContext evalContext);
	Vector getTriggers (); /* vector of TreeReference */
	
	Vector<Object> pivot(FormInstance model, EvaluationContext evalContext) throws UnpivotableExpressionException;
}
