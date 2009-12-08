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

import java.util.Date;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeReference;

public class Recalculate extends Triggerable {
	public Recalculate () {

	}
	
	public Recalculate (IConditionExpr expr, TreeReference contextRef) {
		super(expr, contextRef);
	}
	
	public Recalculate (IConditionExpr expr, TreeReference target, TreeReference contextRef) {
		super(expr, contextRef);
		addTarget(target);
	}
	
	public Object eval (IFormDataModel model, EvaluationContext ec) {
		return expr.evalRaw(model, ec);
	}
	
	public void apply (TreeReference ref, Object result, IFormDataModel model, FormDef f) {
		int dataType = f.getDataModel().resolveReference(ref).dataType;
		f.setAnswer(wrapData(result, dataType), ref);
	}
		
	public boolean canCascade () {
		return true;
	}
	
	public boolean equals (Object o) {
		if (o instanceof Recalculate) {
			Recalculate r = (Recalculate)o;
			if (this == r)
				return true;
			
			return super.equals(r);
		} else {
			return false;
		}			
	}
	
	private static IAnswerData wrapData (Object val, int dataType) {
		if (val instanceof Boolean) {
			return new IntegerData(((Boolean)val).booleanValue() ? 1 : 0);
		} else if (val instanceof Double) {
			double d = ((Double)val).doubleValue();
			if(Double.isNaN(d)) { return null; }
			
			//droos: i think this should be reworked
			//instead, write IntData if integral, DecData if float, regardless of data type
			if(Constants.DATATYPE_DECIMAL == dataType) {
				return new DecimalData(d);
			} else if(Constants.DATATYPE_INTEGER == dataType) {
				return new IntegerData((int)Math.floor(d));
			} 
			return new DecimalData(d);
		} else if (val instanceof String) {
			String s = (String)val;
			return s.length() > 0 ? new StringData(s) : null;
		} else if (val instanceof Date) {
			return new DateData((Date)val);
		} else {
			throw new RuntimeException("unrecognized data type in 'calculate' expression");
		}
	}
}