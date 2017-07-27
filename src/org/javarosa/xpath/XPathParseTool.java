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

package org.javarosa.xpath;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.parser.Lexer;
import org.javarosa.xpath.parser.Parser;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathParseTool {
	public static final String[] xpathClasses = {
			"org.javarosa.xpath.expr.XPathArithExpr",
			"org.javarosa.xpath.expr.XPathBoolExpr",
			"org.javarosa.xpath.expr.XPathCmpExpr",
			"org.javarosa.xpath.expr.XPathEqExpr",
			"org.javarosa.xpath.expr.XPathFilterExpr",
			"org.javarosa.xpath.expr.XPathFuncExpr",
			"org.javarosa.xpath.expr.XPathNumericLiteral",
			"org.javarosa.xpath.expr.XPathNumNegExpr",
			"org.javarosa.xpath.expr.XPathPathExpr",
			"org.javarosa.xpath.expr.XPathStringLiteral",
			"org.javarosa.xpath.expr.XPathUnionExpr",
			"org.javarosa.xpath.expr.XPathVariableReference"
	};

	public static XPathExpression parseXPath (String xpath) throws XPathSyntaxException {
		return Parser.parse(Lexer.lex(xpath));
	}
}
