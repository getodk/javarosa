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

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFilterExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodeFilterExpr extends ASTNode {
	public ASTNodeAbstractExpr expr;
	public Vector<ASTNode> predicates;
	
	public ASTNodeFilterExpr () {
		predicates = new Vector<ASTNode>();
	}
	
	public Vector<ASTNode> getChildren() {
		Vector<ASTNode> v = new Vector<ASTNode>();
		v.addElement(expr);
		for (Enumeration<ASTNode> e = predicates.elements(); e.hasMoreElements(); )
			v.addElement(e.nextElement());
		return v;
	}
	
	public XPathExpression build() throws XPathSyntaxException {
		XPathExpression[] preds = new XPathExpression[predicates.size()];
		for (int i = 0; i < preds.length; i++)
			preds[i] = predicates.elementAt(i).build();
		
		return new XPathFilterExpr(expr.build(), preds);
	}
}
