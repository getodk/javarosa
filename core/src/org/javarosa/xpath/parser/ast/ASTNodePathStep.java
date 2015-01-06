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
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.expr.XPathStep;
import org.javarosa.xpath.parser.Token;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class ASTNodePathStep extends ASTNode {
	public static final int AXIS_TYPE_ABBR = 1;
	public static final int AXIS_TYPE_EXPLICIT = 2;
	public static final int AXIS_TYPE_NULL = 3;
	
	public static final int NODE_TEST_TYPE_QNAME = 1;
	public static final int NODE_TEST_TYPE_WILDCARD = 2;
	public static final int NODE_TEST_TYPE_NSWILDCARD = 3;
	public static final int NODE_TEST_TYPE_ABBR_DOT = 4;
	public static final int NODE_TEST_TYPE_ABBR_DBL_DOT = 5;
	public static final int NODE_TEST_TYPE_FUNC = 6;
	
	public int axisType;
	public int axisVal;
	public int nodeTestType;
	public ASTNodeFunctionCall nodeTestFunc;
	public XPathQName nodeTestQName;
	public String nodeTestNamespace;
	public Vector<ASTNode> predicates;
	
	public ASTNodePathStep () {
		predicates = new Vector<ASTNode>();
	}
	
	public Vector<ASTNode> getChildren() {
		return predicates;
	}

	public XPathExpression build() {
		return null;
	}
	
	public XPathStep getStep () throws XPathSyntaxException {
		if (nodeTestType == NODE_TEST_TYPE_ABBR_DOT) {
			return XPathStep.ABBR_SELF();
		} else if (nodeTestType == NODE_TEST_TYPE_ABBR_DBL_DOT) {
			return XPathStep.ABBR_PARENT();
		} else {
			XPathStep step;
			
			if (axisType == AXIS_TYPE_NULL)
				axisVal = XPathStep.AXIS_CHILD;
			else if (axisType == AXIS_TYPE_ABBR)
				axisVal = XPathStep.AXIS_ATTRIBUTE;
			
			if (nodeTestType == NODE_TEST_TYPE_QNAME)
				step = new XPathStep(axisVal, nodeTestQName);
			else if (nodeTestType == NODE_TEST_TYPE_WILDCARD)
				step = new XPathStep(axisVal, XPathStep.TEST_NAME_WILDCARD);
			else if (nodeTestType == NODE_TEST_TYPE_NSWILDCARD)
				step = new XPathStep(axisVal, nodeTestNamespace);
			else {
				String funcName = nodeTestFunc.name.toString();
				int type;
				if      (funcName.equals("node"))                   type = XPathStep.TEST_TYPE_NODE;
				else if (funcName.equals("text"))                   type = XPathStep.TEST_TYPE_TEXT;
				else if (funcName.equals("comment"))                type = XPathStep.TEST_TYPE_COMMENT;
				else if (funcName.equals("processing-instruction")) type = XPathStep.TEST_TYPE_PROCESSING_INSTRUCTION;
				else throw new RuntimeException();
			
				step = new XPathStep(axisVal, type);
				if (nodeTestFunc.args.size() > 0) {
					step.literal = (String)((ASTNodeAbstractExpr)nodeTestFunc.args.elementAt(0)).getToken(0).val;
				}
			}
			
			XPathExpression[] preds = new XPathExpression[predicates.size()];
			for (int i = 0; i < preds.length; i++)
				preds[i] = ((ASTNode)predicates.elementAt(i)).build();
			step.predicates = preds;
			
			return step;
		}
	}
	
	public static int validateAxisName (String axisName) {
		int axis = -1;

		if      (axisName.equals("child"))              axis = XPathStep.AXIS_CHILD;
		else if (axisName.equals("descendant"))         axis = XPathStep.AXIS_DESCENDANT;
		else if (axisName.equals("parent"))             axis = XPathStep.AXIS_PARENT;
		else if (axisName.equals("ancestor"))           axis = XPathStep.AXIS_ANCESTOR;
		else if (axisName.equals("following-sibling"))  axis = XPathStep.AXIS_FOLLOWING_SIBLING;
		else if (axisName.equals("preceding-sibling"))  axis = XPathStep.AXIS_PRECEDING_SIBLING;
		else if (axisName.equals("following"))          axis = XPathStep.AXIS_FOLLOWING;
		else if (axisName.equals("preceding"))          axis = XPathStep.AXIS_PRECEDING;
		else if (axisName.equals("attribute"))          axis = XPathStep.AXIS_ATTRIBUTE;
		else if (axisName.equals("namespace"))          axis = XPathStep.AXIS_NAMESPACE;
		else if (axisName.equals("self"))               axis = XPathStep.AXIS_SELF;
		else if (axisName.equals("descendant-or-self")) axis = XPathStep.AXIS_DESCENDANT_OR_SELF;
		else if (axisName.equals("ancestor-or-self"))   axis = XPathStep.AXIS_ANCESTOR_OR_SELF;

		return axis;
	}
	
	public static boolean validateNodeTypeTest (ASTNodeFunctionCall f) {
		String name = f.name.toString();
		if (name.equals("node") || name.equals("text") || name.equals("comment") || name.equals("processing-instruction")) {
			if (f.args.size() == 0) {
				return true;
			} else if (name.equals("processing-instruction") && f.args.size() == 1) {
				ASTNodeAbstractExpr x = (ASTNodeAbstractExpr)f.args.elementAt(0);
				return x.content.size() == 1 && x.getTokenType(0) == Token.STR;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
