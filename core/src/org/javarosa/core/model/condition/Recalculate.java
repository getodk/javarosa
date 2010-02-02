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
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
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
	
	public Object eval (FormInstance model, EvaluationContext ec) {
		return expr.evalRaw(model, ec);
	}
	
	public void apply (TreeReference ref, Object result, FormInstance model, FormDef f) {
		int dataType = f.getInstance().resolveReference(ref).dataType;
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
	
	//droos 1/29/10: we need to come up with a consistent rule for whether the resulting data is determined
	//by the type of the instance node, or the type of the expression result. right now it's a mix and a mess
	//note a caveat with going solely by instance node type is that untyped nodes default to string!
	
	//for now, these are the rules:
	// if node type == bool, convert to boolean (for numbers, zero = f, non-zero = t; all other datatypes -> error)
	// if numeric data, convert to int if node type is int OR data is an integer; else convert to double
	// if string data or date data, keep as is
	/**
	 * convert the data object returned by the xpath expression into an IAnswerData suitable for
	 * storage in the FormInstance
	 * 
	 */
	private static IAnswerData wrapData (Object val, int dataType) {
		if (Constants.DATATYPE_BOOLEAN == dataType) {
			//ctsims: We should really be using the boolean datatype for real, it's 
			//necessary for backend calculations and XSD compliance
			
			boolean b;
			
			if (val instanceof Boolean) {
				b = ((Boolean)val).booleanValue();
			} else if (val instanceof Double) {
				Double d = (Double)val;
				b = Math.abs(d.doubleValue()) > 1.0e-12 && !Double.isNaN(d);
			} else {
				throw new RuntimeException("unrecognized data representation while trying to convert to BOOLEAN");
			}

			return new BooleanData(b);
		} else if (val instanceof Double) {
			double d = ((Double)val).doubleValue();
			if (Double.isNaN(d)) {
				return null;
			}
			
			boolean isIntegral = Math.abs(d - (int)d) < 1.0e-9;			
			if(Constants.DATATYPE_INTEGER == dataType || isIntegral) {
				return new IntegerData((int)d);
			} else {
				return new DecimalData(d);
			}
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