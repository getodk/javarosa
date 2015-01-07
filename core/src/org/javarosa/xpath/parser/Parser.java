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

package org.javarosa.xpath.parser;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathQName;
import org.javarosa.xpath.parser.ast.ASTNode;
import org.javarosa.xpath.parser.ast.ASTNodeAbstractExpr;
import org.javarosa.xpath.parser.ast.ASTNodeBinaryOp;
import org.javarosa.xpath.parser.ast.ASTNodeFilterExpr;
import org.javarosa.xpath.parser.ast.ASTNodeFunctionCall;
import org.javarosa.xpath.parser.ast.ASTNodeLocPath;
import org.javarosa.xpath.parser.ast.ASTNodePathStep;
import org.javarosa.xpath.parser.ast.ASTNodePredicate;
import org.javarosa.xpath.parser.ast.ASTNodeUnaryOp;

/* if you try to edit this code, you will likely break it */

public class Parser {

	public static XPathExpression parse (Vector<Token> tokens) throws XPathSyntaxException {
		ASTNode tree = buildParseTree(tokens);
		return tree.build();
	}

	public static ASTNode buildParseTree (Vector<Token> tokens) throws XPathSyntaxException {
		ASTNodeAbstractExpr root = new ASTNodeAbstractExpr();
		for (int i = 0; i < tokens.size(); i++)
			root.content.addElement(tokens.elementAt(i));

		parseFuncCalls(root);
		parseParens(root);
		parsePredicates(root);
		parseOperators(root);
		parsePathExpr(root);
		verifyBaseExpr(root);

		return root;
	}

	private static void parseOperators (ASTNode root) {
		int[] orOp = {Token.OR};
		int[] andOp = {Token.AND};
		int[] eqOps = {Token.EQ, Token.NEQ};
		int[] cmpOps = {Token.LT, Token.LTE, Token.GT, Token.GTE};
		int[] addOps = {Token.PLUS, Token.MINUS};
		int[] multOps = {Token.MULT, Token.DIV, Token.MOD};
		int[] unionOp = {Token.UNION};

		parseBinaryOp(root, orOp, ASTNodeBinaryOp.ASSOC_RIGHT);
		parseBinaryOp(root, andOp, ASTNodeBinaryOp.ASSOC_RIGHT);
		parseBinaryOp(root, eqOps, ASTNodeBinaryOp.ASSOC_LEFT);
		parseBinaryOp(root, cmpOps, ASTNodeBinaryOp.ASSOC_LEFT);
		parseBinaryOp(root, addOps, ASTNodeBinaryOp.ASSOC_LEFT);
		parseBinaryOp(root, multOps, ASTNodeBinaryOp.ASSOC_LEFT);
		parseUnaryOp(root, Token.UMINUS);
		parseBinaryOp(root, unionOp, ASTNodeBinaryOp.ASSOC_LEFT); /* 'a|-b' parses weird (as in, doesn't), but i think that's correct */
	}

	//find and condense all function calls in the current level, then do the same in lower levels
	private static void parseFuncCalls (ASTNode node) throws XPathSyntaxException {
		if (node instanceof ASTNodeAbstractExpr) {
			ASTNodeAbstractExpr absNode = (ASTNodeAbstractExpr)node;

			int i = 0;
			while (i < absNode.content.size() - 1) {
				if (absNode.getTokenType(i + 1) == Token.LPAREN && absNode.getTokenType(i) == Token.QNAME)
					condenseFuncCall(absNode, i);
				i++;
			}
		}

		for (Enumeration e = node.getChildren().elements(); e.hasMoreElements(); ) {
			parseFuncCalls((ASTNode)e.nextElement());
		}
	}

	//i == index of token beginning func call (func name)
	private static void condenseFuncCall (ASTNodeAbstractExpr node, int funcStart) throws XPathSyntaxException {
		ASTNodeFunctionCall funcCall = new ASTNodeFunctionCall((XPathQName)node.getToken(funcStart).val);

		int funcEnd = node.indexOfBalanced(funcStart + 1, Token.RPAREN, Token.LPAREN, Token.RPAREN);
		if (funcEnd == -1) {
			throw new XPathSyntaxException("Mismatched brackets or parentheses"); //mismatched parens
		}

		ASTNodeAbstractExpr.Partition args = node.partitionBalanced(Token.COMMA, funcStart + 1, Token.LPAREN, Token.RPAREN);
		if (args.pieces.size() == 1 && ((ASTNodeAbstractExpr)args.pieces.elementAt(0)).content.size() == 0) {
			//no arguments
		} else {
			//process arguments
			funcCall.args = args.pieces;
		}

		node.condense(funcCall, funcStart, funcEnd + 1);
	}

	private static void parseParens (ASTNode node) throws XPathSyntaxException {
		parseBalanced(node, new SubNodeFactory () {
			public ASTNode newNode (ASTNodeAbstractExpr node) { return node; }
		}, Token.LPAREN, Token.RPAREN);
	}

	private static void parsePredicates (ASTNode node) throws XPathSyntaxException {
		parseBalanced(node, new SubNodeFactory () {
			public ASTNode newNode (ASTNodeAbstractExpr node) {
				ASTNodePredicate p = new ASTNodePredicate();
				p.expr = node;
				return p;
			}
		}, Token.LBRACK, Token.RBRACK);
	}

	private static abstract class SubNodeFactory {
		public abstract ASTNode newNode (ASTNodeAbstractExpr node);
	}

	private static void parseBalanced (ASTNode node, SubNodeFactory snf, int lToken, int rToken) throws XPathSyntaxException {
		if (node instanceof ASTNodeAbstractExpr) {
			ASTNodeAbstractExpr absNode = (ASTNodeAbstractExpr)node;

			int i = 0;
			while (i < absNode.content.size()) {
				int type = absNode.getTokenType(i);
				if (type == rToken) {
					throw new XPathSyntaxException("Unbalanced brackets or parentheses!"); //unbalanced
				} else if (type == lToken) {
					int j = absNode.indexOfBalanced(i, rToken, lToken, rToken);
					if (j == -1) {
						throw new XPathSyntaxException("mismatched brackets or parentheses!"); //mismatched
					}

					absNode.condense(snf.newNode(absNode.extract(i + 1, j)), i, j + 1);
				}
				i++;
			}
		}

		for (Enumeration e = node.getChildren().elements(); e.hasMoreElements(); ) {
			parseBalanced((ASTNode)e.nextElement(), snf, lToken, rToken);
		}
	}

	private static void parseBinaryOp (ASTNode node, int[] ops, int associativity) {
		if (node instanceof ASTNodeAbstractExpr) {
			ASTNodeAbstractExpr absNode = (ASTNodeAbstractExpr)node;
			ASTNodeAbstractExpr.Partition part = absNode.partition(ops, 0, absNode.content.size());

			if (part.separators.size() == 0) {
				//no occurrences of op
			} else {
				ASTNodeBinaryOp binOp = new ASTNodeBinaryOp();
				binOp.associativity = associativity;
				binOp.exprs = part.pieces;
				binOp.ops = part.separators;

				absNode.condense(binOp, 0, absNode.content.size());
			}
		}

		for (Enumeration e = node.getChildren().elements(); e.hasMoreElements(); ) {
			parseBinaryOp((ASTNode)e.nextElement(), ops, associativity);
		}
	}

	private static void parseUnaryOp (ASTNode node, int op) {
		if (node instanceof ASTNodeAbstractExpr) {
			ASTNodeAbstractExpr absNode = (ASTNodeAbstractExpr)node;

			if (absNode.content.size() > 0 && absNode.getTokenType(0) == op) {
				ASTNodeUnaryOp unOp = new ASTNodeUnaryOp();
				unOp.op = op;
				unOp.expr = (absNode.content.size() > 1 ? absNode.extract(1, absNode.content.size()) : new ASTNodeAbstractExpr());
				absNode.condense(unOp, 0, absNode.content.size());
			}
		}

		for (Enumeration e = node.getChildren().elements(); e.hasMoreElements(); ) {
			parseUnaryOp((ASTNode)e.nextElement(), op);
		}
	}

	private static void parsePathExpr (ASTNode node) throws XPathSyntaxException {
		if (node instanceof ASTNodeAbstractExpr) {
			ASTNodeAbstractExpr absNode = (ASTNodeAbstractExpr)node;
			int[] pathOps = {Token.SLASH, Token.DBL_SLASH};
			ASTNodeAbstractExpr.Partition part = absNode.partition(pathOps, 0, absNode.content.size());

			if (part.separators.size() == 0) {
				//filter expression or standalone step
				if (isStep(absNode)) {
					ASTNodePathStep step = parseStep(absNode);
					ASTNodeLocPath path = new ASTNodeLocPath();
					path.clauses.addElement(step);
					absNode.condense(path, 0, absNode.content.size());
				} else {
					//filter expr
					ASTNodeFilterExpr filt = parseFilterExp(absNode);
					if (filt != null) {
						absNode.condense(filt, 0, absNode.content.size());
					}
				}
			} else {
				//path expression (but first clause may be filter expr)
				ASTNodeLocPath path = new ASTNodeLocPath();
				path.separators = part.separators;

				if (part.separators.size() == 1 && absNode.content.size() == 1 && vectInt(part.separators, 0) == Token.SLASH) {
					//empty absolute path
				} else {
					for (int i = 0; i < part.pieces.size(); i++) {
						ASTNodeAbstractExpr x = (ASTNodeAbstractExpr)part.pieces.elementAt(i);
						if (isStep(x)) {
							ASTNodePathStep step = parseStep(x);
							path.clauses.addElement(step);
						} else {
							if (i == 0) {
								if (x.content.size() == 0) {
									//absolute path expr; first clause is null
									/* do nothing */
								} else {
									//filter expr
									ASTNodeFilterExpr filt = parseFilterExp(x);
									if (filt != null)
										path.clauses.addElement(filt);
									else
										path.clauses.addElement(x);
								}
							} else {
								throw new XPathSyntaxException("Unexpected beginning of path");
							}
						}
					}
				}
				absNode.condense(path, 0, absNode.content.size());
			}
		}

		for (Enumeration e = node.getChildren().elements(); e.hasMoreElements(); ) {
			parsePathExpr((ASTNode)e.nextElement());
		}
	}

	//true if 'node' is potentially a step, as opposed to a filter expr
	private static boolean isStep (ASTNodeAbstractExpr node) {
		if (node.content.size() > 0) {
			int type = node.getTokenType(0);
			if (type == Token.QNAME ||
				type == Token.WILDCARD ||
				type == Token.NSWILDCARD ||
				type == Token.AT ||
				type == Token.DOT ||
				type == Token.DBL_DOT) {
				return true;
			} else if (node.content.elementAt(0) instanceof ASTNodeFunctionCall) {
				String name = ((ASTNodeFunctionCall)node.content.elementAt(0)).name.toString();
				return (name.equals("node") || name.equals("text") || name.equals("comment") || name.equals("processing-instruction"));
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	//please kill me
	private static ASTNodePathStep parseStep (ASTNodeAbstractExpr node) throws XPathSyntaxException {
		ASTNodePathStep step = new ASTNodePathStep();
		if (node.content.size() == 1 && node.getTokenType(0) == Token.DOT) {
			step.axisType = ASTNodePathStep.AXIS_TYPE_NULL;
			step.nodeTestType = ASTNodePathStep.NODE_TEST_TYPE_ABBR_DOT;
		} else if (node.content.size() == 1 && node.getTokenType(0) == Token.DBL_DOT) {
			step.axisType = ASTNodePathStep.AXIS_TYPE_NULL;
			step.nodeTestType = ASTNodePathStep.NODE_TEST_TYPE_ABBR_DBL_DOT;
		} else {
			int i = 0;
			if (node.content.size() > 0 && node.getTokenType(0) == Token.AT) {
				step.axisType = ASTNodePathStep.AXIS_TYPE_ABBR;
				i += 1;
			} else if (node.content.size() > 1 && node.getTokenType(0) == Token.QNAME && node.getTokenType(1) == Token.DBL_COLON) {
				int axisVal = ASTNodePathStep.validateAxisName(((XPathQName)node.getToken(0).val).toString());
				if (axisVal == -1) {
					throw new XPathSyntaxException("Invalid Axis: " + ((XPathQName)node.getToken(0).val).toString());
				}
				step.axisType = ASTNodePathStep.AXIS_TYPE_EXPLICIT;
				step.axisVal = axisVal;
				i += 2;
			} else {
				step.axisType = ASTNodePathStep.AXIS_TYPE_NULL;
			}

			if (node.content.size() > i && node.getTokenType(i) == Token.WILDCARD) {
				step.nodeTestType = ASTNodePathStep.NODE_TEST_TYPE_WILDCARD;
			} else if (node.content.size() > i && node.getTokenType(i) == Token.NSWILDCARD) {
				step.nodeTestType = ASTNodePathStep.NODE_TEST_TYPE_NSWILDCARD;
				step.nodeTestNamespace = (String)node.getToken(i).val;
			} else if (node.content.size() > i && node.getTokenType(i) == Token.QNAME) {
				step.nodeTestType = ASTNodePathStep.NODE_TEST_TYPE_QNAME;
				step.nodeTestQName = (XPathQName)node.getToken(i).val;
			} else if (node.content.size() > i && node.content.elementAt(i) instanceof ASTNodeFunctionCall) {
				if (!ASTNodePathStep.validateNodeTypeTest((ASTNodeFunctionCall)node.content.elementAt(i))) {
					throw new XPathSyntaxException();
				}
				step.nodeTestType = ASTNodePathStep.NODE_TEST_TYPE_FUNC;
				step.nodeTestFunc = (ASTNodeFunctionCall)node.content.elementAt(i);
			} else {
				throw new XPathSyntaxException();
			}
			i += 1;

			while (i < node.content.size()) {
				if (node.content.elementAt(i) instanceof ASTNodePredicate) {
					step.predicates.addElement((ASTNode) node.content.elementAt(i));
				} else {
					throw new XPathSyntaxException();
				}
				i++;
			}
		}

		return step;
	}

	private static ASTNodeFilterExpr parseFilterExp (ASTNodeAbstractExpr node) throws XPathSyntaxException {
		ASTNodeFilterExpr filt = new ASTNodeFilterExpr();
		int i;
		for (i = node.content.size() - 1; i >= 0; i--) {
			if (node.content.elementAt(i) instanceof ASTNodePredicate) {
				filt.predicates.insertElementAt((ASTNode) node.content.elementAt(i), 0);
			} else {
				break;
			}
		}

		if (filt.predicates.size() == 0)
			return null;

		filt.expr = node.extract(0, i + 1);
		return filt;
	}

	public static void verifyBaseExpr (ASTNode node) throws XPathSyntaxException {
		if (node instanceof ASTNodeAbstractExpr) {
			ASTNodeAbstractExpr absNode = (ASTNodeAbstractExpr)node;

			if (!absNode.isNormalized()) {
				throw new XPathSyntaxException("Bad node: " + absNode.toString());
			}
		}

		for (Enumeration e = node.getChildren().elements(); e.hasMoreElements(); ) {
			verifyBaseExpr((ASTNode)e.nextElement());
		}
	}

	public static int vectInt (Vector<Integer> v, int i) {
		return v.elementAt(i).intValue();
	}
}
