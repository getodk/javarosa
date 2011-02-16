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
import java.util.Enumeration;
import java.util.Hashtable;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.xpath.IExprDataType;

/* a collection of objects that affect the evaluation of an expression, like function handlers
 * and (not supported) variable bindings
 */
public class EvaluationContext {
	private TreeReference contextNode; //unambiguous ref used as the anchor for relative paths
	private Hashtable functionHandlers;
	private Hashtable variables;
	
	public boolean isConstraint; //true if we are evaluating a constraint
	public IAnswerData candidateValue; //if isConstraint, this is the value being validated
	public boolean isCheckAddChild; //if isConstraint, true if we are checking the constraint of a parent node on how
									//  many children it may have
	
	
	public EvaluationContext (EvaluationContext base, TreeReference context) {
		this.functionHandlers = base.functionHandlers;
		this.contextNode = context;
		this.variables = new Hashtable();
	}
	
	public EvaluationContext () {
		functionHandlers = new Hashtable();
		variables = new Hashtable();
	}
	
	public TreeReference getContextRef () {
		return contextNode;
	}
	
	public void addFunctionHandler (IFunctionHandler fh) {
		functionHandlers.put(fh.getName(), fh);
	}
	
	public Hashtable getFunctionHandlers () {
		return functionHandlers;
	}
	
	public void setVariables(Hashtable<String, ?> variables) {
		for (Enumeration e = variables.keys(); e.hasMoreElements(); ) {
			String var = (String)e.nextElement();
			setVariable(var, variables.get(var));
		}
	}
	
	public void setVariable(String name, Object value) {
		//No such thing as a null xpath variable. Empty
		//values in XPath just get converted to ""
		if(value == null) {
			variables.put(name, "");
			return;
		}
		//Otherwise check whether the value is one of the normal first
		//order datatypes used in xpath evaluation
		if(value instanceof Boolean ||
				   value instanceof Double  ||
				   value instanceof String  ||
				   value instanceof Date    ||
				   value instanceof IExprDataType) {
				variables.put(name, value);
				return;
		}
		
		//Some datatypes can be trivially converted to a first order
		//xpath datatype
		if(value instanceof Integer) {
			variables.put(name, new Double(((Integer)value).doubleValue()));
			return;
		}
		if(value instanceof Float) {
			variables.put(name, new Double(((Float)value).doubleValue()));
			return;
		}
		
		//Otherwise we just hope for the best, I suppose? Should we log this?
		else {
			variables.put(name, value);
		}
	}
	
	public Object getVariable(String name) {
		return variables.get(name);
	}
}
