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

package org.javarosa.xpath.parser.ast;

import java.util.Vector;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFilterExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.parser.Parser;
import org.javarosa.xpath.parser.Token;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodeLocPath extends ASTNode {
	public Vector<ASTNode> clauses;
	public Vector<Integer> separators;
	
	public ASTNodeLocPath () {
		clauses = new Vector<ASTNode>();
		separators = new Vector<Integer>();
	}
	
	public Vector<ASTNode> getChildren() {
		return clauses;
	}
	
	public boolean isAbsolute () {
		return (clauses.size() == separators.size()) || (clauses.size() == 0 && separators.size() == 1);
	}
	
	public XPathExpression build() throws XPathSyntaxException {
		Vector<XPathStep> steps = new Vector<XPathStep>();
		XPathExpression filtExpr = null;
		int offset = isAbsolute() ? 1 : 0;
		for (int i = 0; i < clauses.size() + offset; i++) {
			if (offset == 0 || i > 0) {
				if (clauses.elementAt(i - offset) instanceof ASTNodePathStep) {
					steps.addElement(((ASTNodePathStep)clauses.elementAt(i - offset)).getStep());
				} else {
					filtExpr = clauses.elementAt(i - offset).build();
				}
			}
				
			if (i < separators.size()) {
				if (Parser.vectInt(separators, i) == Token.DBL_SLASH) {
					steps.addElement(XPathStep.ABBR_DESCENDANTS());
				}
			}
		}

		XPathStep[] stepArr = new XPathStep[steps.size()];
		for (int i = 0; i < stepArr.length; i++)
			stepArr[i] = steps.elementAt(i);
		
		if (filtExpr == null) {
			return new XPathPathExpr(isAbsolute() ? XPathPathExpr.INIT_CONTEXT_ROOT : XPathPathExpr.INIT_CONTEXT_RELATIVE, stepArr);
		} else {
			if (filtExpr instanceof XPathFilterExpr) {
				return new XPathPathExpr((XPathFilterExpr)filtExpr, stepArr);
			} else {
				return new XPathPathExpr(new XPathFilterExpr(filtExpr, new XPathExpression[0]), stepArr);
			}
		}		
	}
}
