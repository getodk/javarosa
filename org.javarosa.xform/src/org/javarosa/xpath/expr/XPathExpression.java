package org.javarosa.xpath.expr;

public abstract class XPathExpression {

	/*======= DEBUGGING ========*/
	// do not compile onto phone

	int indent;

	private void printStr (String s) {
		for (int i = 0; i < 2 * indent; i++)
			System.out.print(" ");
		System.out.println(s);
	}

	public void printParseTree () {
		indent = -1;
		print(this);
	}

	public void print (Object o) {
		indent += 1;

		if (o instanceof XPathStringLiteral) {
			XPathStringLiteral x = (XPathStringLiteral)o;
			printStr("strlit {" + x.s + "}");
		} else if (o instanceof XPathNumericLiteral) {
			XPathNumericLiteral x = (XPathNumericLiteral)o;
			printStr("numlit {" + x.d + "}");
		} else if (o instanceof XPathVariableReference) {
			XPathVariableReference x = (XPathVariableReference)o;
			printStr("var {" + x.id.toString() + "}");
		} else if (o instanceof XPathArithExpr) {
			XPathArithExpr x = (XPathArithExpr)o;
			String op = null;
			switch(x.op) {
			case XPathArithExpr.ADD: op = "add"; break;
			case XPathArithExpr.SUBTRACT: op = "subtr"; break;
			case XPathArithExpr.MULTIPLY: op = "mult"; break;
			case XPathArithExpr.DIVIDE: op = "div"; break;
			case XPathArithExpr.MODULO: op = "mod"; break;
			}
			printStr(op + " {{");
			print(x.a);
			printStr(" } {");
			print(x.b);
			printStr("}}");
		} else if (o instanceof XPathBoolExpr) {
			XPathBoolExpr x = (XPathBoolExpr)o;
			String op = null;
			switch(x.op) {
			case XPathBoolExpr.AND: op = "and"; break;
			case XPathBoolExpr.OR: op = "or"; break;
			}
			printStr(op + " {{");
			print(x.a);
			printStr(" } {");
			print(x.b);
			printStr("}}");
		} else if (o instanceof XPathCmpExpr) {
			XPathCmpExpr x = (XPathCmpExpr)o;
			String op = null;
			switch(x.op) {
			case XPathCmpExpr.LT: op = "lt"; break;
			case XPathCmpExpr.LTE: op = "lte"; break;
			case XPathCmpExpr.GT: op = "gt"; break;
			case XPathCmpExpr.GTE: op = "gte"; break;
			}
			printStr(op + " {{");
			print(x.a);
			printStr(" } {");
			print(x.b);
			printStr("}}");
		} else if (o instanceof XPathEqExpr) {
			XPathEqExpr x = (XPathEqExpr)o;
			String op = x.equal ? "eq" : "neq";
			printStr(op + " {{");
			print(x.a);
			printStr(" } {");
			print(x.b);
			printStr("}}");
		} else if (o instanceof XPathUnionExpr) {
			XPathUnionExpr x = (XPathUnionExpr)o;
			printStr("union {{");
			print(x.a);
			printStr(" } {");
			print(x.b);
			printStr("}}");
		} else if (o instanceof XPathNumNegExpr) {
			XPathNumNegExpr x = (XPathNumNegExpr)o;
			printStr("neg {");
			print(x.a);
			printStr("}");
		} else if (o instanceof XPathFuncExpr) {
			XPathFuncExpr x = (XPathFuncExpr)o;
			if (x.args == null) {
				printStr("func {" + x.id.toString() + ", args {none}}");
			} else {
				printStr("func {" + x.id.toString() + ", args {{");
				for (int i = 0; i < x.args.length; i++) {
					print(x.args[i]);
					if (i < x.args.length - 1)
						printStr(" } {");
				}
				printStr("}}}");
			}
		} else if (o instanceof XPathPathExpr) {
			XPathPathExpr x = (XPathPathExpr)o;
			String init = null;

			switch (x.init_context) {
			case XPathPathExpr.INIT_CONTEXT_ROOT: init = "root"; break;
			case XPathPathExpr.INIT_CONTEXT_RELATIVE: init = "relative"; break;
			case XPathPathExpr.INIT_CONTEXT_EXPR: init = "expr"; break;
			}

			printStr("path {init-context:" + init + ",");

			if (x.init_context == XPathPathExpr.INIT_CONTEXT_EXPR) {
				printStr(" init-expr:{{");
				print(x.expr);

				if (x.predicates.length == 0) {
					printStr(" } predicates {none}}");
				} else {
					printStr(" } predicates {{");
					for (int i = 0; i < x.predicates.length; i++) {
						print(x.predicates[i]);
						if (i < x.predicates.length - 1)
							printStr(" } {");
					}
					printStr(" }}}");
				}
			}

			if (x.steps.length == 0) {
				printStr(" steps {none}");
				printStr("}");
			} else {
				printStr(" steps {{");
				for (int i = 0; i < x.steps.length; i++) {
					print(x.steps[i]);
					if (i < x.steps.length - 1)
						printStr(" } {");
				}
				printStr("}}}");
			}

		} else if (o instanceof XPathStep) {
			XPathStep x = (XPathStep)o;
			String axis = null;
			String test = null;

			switch (x.axis) {
			case XPathStep.AXIS_CHILD: axis = "child"; break;
			case XPathStep.AXIS_DESCENDANT: axis = "descendant"; break;
			case XPathStep.AXIS_PARENT: axis = "parent"; break;
			case XPathStep.AXIS_ANCESTOR: axis = "ancestor"; break;
			case XPathStep.AXIS_FOLLOWING_SIBLING: axis = "following-sibling"; break;
			case XPathStep.AXIS_PRECEDING_SIBLING: axis = "preceding-sibling"; break;
			case XPathStep.AXIS_FOLLOWING: axis = "following"; break;
			case XPathStep.AXIS_PRECEDING: axis = "preceding"; break;
			case XPathStep.AXIS_ATTRIBUTE: axis = "attribute"; break;
			case XPathStep.AXIS_NAMESPACE: axis = "namespace"; break;
			case XPathStep.AXIS_SELF: axis = "self"; break;
			case XPathStep.AXIS_DESCENDANT_OR_SELF: axis = "descendant-or-self"; break;
			case XPathStep.AXIS_ANCESTOR_OR_SELF: axis = "ancestor-or-self"; break;
			}

			switch(x.test) {
			case XPathStep.TEST_NAME: test = x.name.toString(); break;
			case XPathStep.TEST_NAME_WILDCARD: test = "*"; break;
			case XPathStep.TEST_NAMESPACE_WILDCARD: test = x.namespace + ":*"; break;
			case XPathStep.TEST_TYPE_NODE: test = "node()"; break;
			case XPathStep.TEST_TYPE_TEXT: test = "text()"; break;
			case XPathStep.TEST_TYPE_COMMENT: test = "comment()"; break;
			case XPathStep.TEST_TYPE_PROCESSING_INSTRUCTION: test = "proc-instr(" + x.literal + ")"; break;
			}

			if (x.predicates.length == 0) {
				printStr("step {axis:" + axis + " test:" + test + " predicates {none}}");
			} else {
				printStr("step {axis:" + axis + " test:" + test + " predicates {{");
				for (int i = 0; i < x.predicates.length; i++) {
					print(x.predicates[i]);
					if (i < x.predicates.length - 1)
						printStr(" } {");
				}
				printStr("}}}");
			}
		}

		indent -= 1;
	}
}
