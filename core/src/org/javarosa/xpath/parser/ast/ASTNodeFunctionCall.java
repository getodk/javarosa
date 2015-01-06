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
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodeFunctionCall extends ASTNode {
	public XPathQName name;
	public Vector<ASTNode> args;
	
	public ASTNodeFunctionCall (XPathQName name) {
		this.name = name;
		args = new Vector<ASTNode>();
	}
	
	public Vector<ASTNode> getChildren () {
		return args;
	}
	
	public XPathExpression build() throws XPathSyntaxException {
		XPathExpression[] xargs = new XPathExpression[args.size()];
		for (int i = 0; i < args.size(); i++)
			xargs[i] = args.elementAt(i).build();
		
		return new XPathFuncExpr(name, xargs);
	}
}
