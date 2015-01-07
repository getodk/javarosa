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
import org.javarosa.xpath.expr.XPathNumNegExpr;
import org.javarosa.xpath.expr.XPathUnaryOpExpr;
import org.javarosa.xpath.parser.Token;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodeUnaryOp extends ASTNode {
	public ASTNode expr;
	public int op;
		
	public Vector<ASTNode> getChildren () {
		Vector<ASTNode> v = new Vector<ASTNode>();
		v.addElement(expr);
		return v;
	}

	public XPathExpression build() throws XPathSyntaxException {
		XPathUnaryOpExpr x;
		if (op == Token.UMINUS) {
			x = new XPathNumNegExpr(expr.build());
		} else {
			throw new XPathSyntaxException();
		}
		return x;
	}
}
