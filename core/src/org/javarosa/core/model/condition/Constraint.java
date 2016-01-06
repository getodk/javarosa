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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.services.Logger;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;

public class Constraint implements Externalizable {
	public IConditionExpr constraint;
	private String constraintMsg;
	private XPathExpression xPathConstraintMsg;
	
	public Constraint () { }
	
	public Constraint (IConditionExpr constraint, String constraintMsg) {
		this.constraint = constraint;
		this.constraintMsg = constraintMsg;
		attemptConstraintCompile();
	}
	
	public String getConstraintMessage(EvaluationContext ec, FormInstance instance, String textForm) {
		if(xPathConstraintMsg == null) {
			//If the request is for getting a constraint message in a specific format (like audio) from 
			//itext, and there's no xpath, we couldn't possibly fulfill it
			return textForm == null ? constraintMsg : null;
		} else{
        	if(textForm != null) {
        		ec.setOutputTextForm(textForm);
        	} 
			try{
				Object value = xPathConstraintMsg.eval(instance, ec);
				if(value != null && ((String) value).length() != 0) {
					return (String)value;
				}
				return null;
			} catch(Exception e) {
				Logger.exception("Error evaluating a valid-looking constraint xpath ", e);
				return constraintMsg;
			}
		}
	}
	
	private void attemptConstraintCompile() {
		xPathConstraintMsg = null;
		try {
			if(constraintMsg != null) {
				xPathConstraintMsg = XPathParseTool.parseXPath("string(" + constraintMsg + ")");
			}
		} catch(Exception e) {
			//Expected in probably most cases.
		}
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		constraint = (IConditionExpr)ExtUtil.read(in, new ExtWrapTagged(), pf);
		constraintMsg = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		attemptConstraintCompile();
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(constraint));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(constraintMsg));
	}
}
