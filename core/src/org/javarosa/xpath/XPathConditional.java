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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.javarosa.core.log.FatalException;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.IConditionExpr;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.expr.XPathBinaryOpExpr;
import org.javarosa.xpath.expr.XPathExpression;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.javarosa.xpath.expr.XPathPathExpr;
import org.javarosa.xpath.expr.XPathUnaryOpExpr;
import org.javarosa.xpath.parser.XPathSyntaxException;

public class XPathConditional implements IConditionExpr {
	private XPathExpression expr;
	public String xpath; //not serialized!
	public boolean hasNow; //indicates whether this XpathConditional contains the now() function (used for timestamping)

	public XPathConditional (String xpath) throws XPathSyntaxException {
		hasNow = false;
		if(xpath.indexOf("now()") > -1) {
			hasNow = true;
		}
		this.expr = XPathParseTool.parseXPath(xpath);
		this.xpath = xpath;
	}

	public XPathConditional (XPathExpression expr) {
		this.expr = expr;
	}

	public XPathConditional () {

	}

	public XPathExpression getExpr () {
		return expr;
	}

	public Object evalRaw (FormInstance model, EvaluationContext evalContext) {
		try{
			return XPathFuncExpr.unpack(expr.eval(model, evalContext));
		} catch(XPathUnsupportedException e){
			if(xpath != null){
				throw new XPathUnsupportedException(xpath);
			}else{
				throw e;
			}


		}
	}

	public boolean eval (FormInstance model, EvaluationContext evalContext) {
		return XPathFuncExpr.toBoolean(evalRaw(model, evalContext)).booleanValue();
	}

	public String evalReadable (FormInstance model, EvaluationContext evalContext) {
		return XPathFuncExpr.toString(evalRaw(model, evalContext));
	}

	public List<TreeReference> evalNodeset (FormInstance model, EvaluationContext evalContext) {
		if (expr instanceof XPathPathExpr) {
			return ((XPathPathExpr)expr).eval(model, evalContext).getReferences();
		} else {
			throw new FatalException("evalNodeset: must be path expression");
		}
	}

	public Set<TreeReference> getTriggers (TreeReference contextRef) {
		Set<TreeReference> triggers = new HashSet<TreeReference>();
		getTriggers(expr, triggers, contextRef);
		return triggers;
	}

	private static void getTriggers (XPathExpression x, Set<TreeReference> v, TreeReference contextRef) {
		if (x instanceof XPathPathExpr) {
			TreeReference ref = ((XPathPathExpr)x).getReference();
			TreeReference contextualized = ref;
			if(contextRef != null) {
				contextualized = ref.contextualize(contextRef);
			}

			//TODO: It's possible we should just handle this the same way as "genericize". Not entirely clear.
			if(contextualized.hasPredicates()) {
				contextualized = contextualized.removePredicates();
			}

			v.add(contextualized);

			for(int i = 0; i < ref.size() ; i++) {
            List<XPathExpression> predicates = ref.getPredicate(i);
				if(predicates == null) {
					continue;
				}

				//we can't generate this properly without an absolute reference
				if(!ref.isAbsolute()) { throw new IllegalArgumentException("can't get triggers for relative references");}
				TreeReference predicateContext = ref.getSubReference(i);

				for(XPathExpression predicate : predicates) {
					getTriggers(predicate, v, predicateContext);
				}
			}
		} else if (x instanceof XPathBinaryOpExpr) {
			getTriggers(((XPathBinaryOpExpr)x).a, v, contextRef);
			getTriggers(((XPathBinaryOpExpr)x).b, v, contextRef);
		} else if (x instanceof XPathUnaryOpExpr) {
			getTriggers(((XPathUnaryOpExpr)x).a, v, contextRef);
		} else if (x instanceof XPathFuncExpr) {
			XPathFuncExpr fx = (XPathFuncExpr)x;
			for (int i = 0; i < fx.args.length; i++)
				getTriggers(fx.args[i], v, contextRef);
		}
	}

	public boolean equals (Object o) {
		if (o instanceof XPathConditional) {
			XPathConditional cond = (XPathConditional)o;
			return expr.equals(cond.expr);
		} else {
			return false;
		}
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		expr = (XPathExpression)ExtUtil.read(in, new ExtWrapTagged(), pf);
		hasNow = (boolean)ExtUtil.readBool(in);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapTagged(expr));
		ExtUtil.writeBool(out, hasNow);
	}

	public String toString () {
		return "xpath[" + expr.toString() + "]";
	}

	public List<Object> pivot(FormInstance model, EvaluationContext evalContext) throws UnpivotableExpressionException {
		return expr.pivot(model, evalContext);
	}
}
