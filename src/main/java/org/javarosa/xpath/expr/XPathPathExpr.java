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

package org.javarosa.xpath.expr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.pivot.UnpivotableExpressionException;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoTraceData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.DataInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xform.util.XFormAnswerDataSerializer;
import org.javarosa.xpath.XPathNodeset;
import org.javarosa.xpath.XPathTypeMismatchException;
import org.javarosa.xpath.XPathUnsupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XPathPathExpr extends XPathExpression {
    private static final Logger logger = LoggerFactory.getLogger(XPathPathExpr.class.getSimpleName());

    public static final int INIT_CONTEXT_ROOT = 0;
    public static final int INIT_CONTEXT_RELATIVE = 1;
    public static final int INIT_CONTEXT_EXPR = 2;

    public int init_context;
    public XPathStep[] steps;

    //for INIT_CONTEXT_EXPR only
    public XPathFilterExpr filtExpr;

    public XPathPathExpr() {
    } //for deserialization

    public XPathPathExpr(int init_context, XPathStep[] steps) {
        this.init_context = init_context;
        this.steps = steps;
    }

    public XPathPathExpr(XPathFilterExpr filtExpr, XPathStep[] steps) {
        this(INIT_CONTEXT_EXPR, steps);
        this.filtExpr = filtExpr;
    }

    /**
     * translate an xpath path reference into a TreeReference
     * TreeReferences only support a subset of true xpath paths; restrictions are:
     * simple child name tests 'child::name', '.', and '..' allowed only
     * no predicates
     * all '..' steps must come before anything else
     */
    public TreeReference getReference() throws XPathUnsupportedException {
        final TreeReference ref = new TreeReference();
        boolean parentsAllowed;
        switch (init_context) {
            case XPathPathExpr.INIT_CONTEXT_ROOT:
                ref.setRefLevel(TreeReference.REF_ABSOLUTE);
                parentsAllowed = false;
                break;
            case XPathPathExpr.INIT_CONTEXT_RELATIVE:
                ref.setRefLevel(0);
                parentsAllowed = true;
                break;
            case XPathPathExpr.INIT_CONTEXT_EXPR:
                if (filtExpr.x instanceof XPathFuncExpr) {
                    XPathFuncExpr func = (XPathFuncExpr) (this.filtExpr.x);
                    switch (func.id.toString()) {
                        case "instance":
                            ref.setRefLevel(TreeReference.REF_ABSOLUTE); //i assume when refering the non main instance you have to be absolute

                            parentsAllowed = false;
                            if (func.args.length != 1) {
                                throw new XPathUnsupportedException("instance() function used with " + func.args.length + " arguments. Expecting 1 argument");
                            }
                            if (!(func.args[0] instanceof XPathStringLiteral)) {
                                throw new XPathUnsupportedException("instance() function expecting 1 string literal argument");
                            }
                            XPathStringLiteral strLit = (XPathStringLiteral) (func.args[0]);
                            //we've got a non-standard instance in play, watch out
                            if (strLit.s == null) {
                                // absolute reference to the main instance
                                ref.setContext(TreeReference.CONTEXT_ABSOLUTE);
                                ref.setInstanceName(null);
                            } else {
                                ref.setContext(TreeReference.CONTEXT_INSTANCE);
                                ref.setInstanceName(strLit.s);
                            }
                            break;
                        case "current":
                            /*
                             * Notes about the current() function:
                             *
                             * - current() in a calculate should refer to the node it is in.
                             *   This means that to refer to a sibling node, the path should be current()/../<name of sibling node>.
                             *
                             * - current() in a choice filter should refer to the select node the choice filter is called from,
                             *   NOT the expression it is in.
                             *   See https://developer.mozilla.org/en-US/docs/Web/XPath/Functions/current
                             *   (this is the difference between current() and .)
                             *
                             * These cases have been tested and documented with specific examples
                             * in the XPathPathExprCurrentTest test class
                             */
                            parentsAllowed = true;
                            ref.setContext(TreeReference.CONTEXT_ORIGINAL);
                            break;
                        default:
                            //We only support expression root contexts for instance refs, everything else is an illegal filter
                            throw new XPathUnsupportedException("filter expression");
                    }
                } else {
                    //We only support expression root contexts for instance refs, everything else is an illegal filter
                    throw new XPathUnsupportedException("filter expression");
                }

                break;
            default:
                throw new XPathUnsupportedException("filter expression");
        }
        for (int i = 0; i < steps.length; i++) {
            final XPathStep step = steps[i];
            switch (step.axis) {
                case XPathStep.AXIS_SELF:
                    if (step.test != XPathStep.TEST_TYPE_NODE) {
                        throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
                    }
                    break;
                case XPathStep.AXIS_PARENT:
                    if (!parentsAllowed || step.test != XPathStep.TEST_TYPE_NODE) {
                        throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
                    } else {
                        ref.incrementRefLevel();
                    }
                    break;
                case XPathStep.AXIS_ATTRIBUTE:
                    if (step.test == XPathStep.TEST_NAME) {
                        ref.add(step.name.toString(), TreeReference.INDEX_ATTRIBUTE);
                        parentsAllowed = false;
                        //TODO: Can you step back from an attribute, or should this always be
                        //the last step?
                    } else {
                        throw new XPathUnsupportedException("attribute step other than 'attribute::name");
                    }
                    break;
                case XPathStep.AXIS_CHILD:
                    if (step.test == XPathStep.TEST_NAME) {
                        ref.add(step.name.toString(), TreeReference.INDEX_UNBOUND);
                        parentsAllowed = true;
                    } else if (step.test == XPathStep.TEST_NAME_WILDCARD) {
                        ref.add(TreeReference.NAME_WILDCARD, TreeReference.INDEX_UNBOUND);
                        parentsAllowed = true;
                    } else {
                        throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
                    }
                    break;
                default:
                    throw new XPathUnsupportedException("step other than 'child::name', '.', '..'");
            }

            if (step.predicates.length > 0) {
                List<XPathExpression> v = new ArrayList<XPathExpression>(step.predicates.length);
                Collections.addAll(v, step.predicates);
                //int level = ref.getRefLevel() > 0 ? i - ref.getRefLevel() : i; // refLevel represents parenting steps
                ref.addPredicate(i, v);
            }
        }
        return ref;
    }

    public XPathNodeset eval(DataInstance unusedDataInstance, EvaluationContext ec) {
        return new XPathPathExprEval().eval(getReference(), ec);
    }

    public static Object getRefValue(DataInstance model, EvaluationContext ec, TreeReference ref) {
        if (ec.isConstraint && ref.equals(ec.getContextRef())) {
            //ITEMSET TODO: need to update this; for itemset/copy constraints, need to simulate a whole xml sub-tree here
            Object result = unpackValue(ec.candidateValue);
            logger.trace("getRefValue returning candidate value {} for {}", result, ref);
            return result;
        }
        AbstractTreeElement node = model.resolveReference(ref);
        if (node == null) {
            //shouldn't happen -- only existent nodes should be in nodeset
            throw new XPathTypeMismatchException("Node " + ref.toString() + " does not exist!");
        }

        IAnswerData maybeNodeValue = node.isRelevant() ? node.getValue() : null;
        Object result = unpackValue(maybeNodeValue);
        if (maybeNodeValue == null) {
            logger.trace("getRefValue returning empty node value for {}", ref);
        } else {
            logger.trace("getRefValue returning node value {} for {}", result, ref);
        }
        return result;
    }

    public static Object unpackValue(IAnswerData val) {
        if (val == null) {
            return "";
        } else if (val instanceof UncastData) {
            return val.getValue();
        } else if (val instanceof IntegerData) {
            return ((Integer) val.getValue()).doubleValue();
        } else if (val instanceof LongData) {
            return ((Long) val.getValue()).doubleValue();
        } else if (val instanceof DecimalData) {
            return val.getValue();
        } else if (val instanceof StringData) {
            return val.getValue();
        } else if (val instanceof SelectOneData) {
            return ((Selection) val.getValue()).getValue();
        } else if (val instanceof MultipleItemsData) {
            return (new XFormAnswerDataSerializer()).serializeAnswerData(val);
        } else if (val instanceof DateData) {
            return val.getValue();
        } else if (val instanceof DateTimeData) {
            return val.getValue();
        } else if (val instanceof TimeData) {
            return val.getValue();
        } else if (val instanceof BooleanData) {
            return val.getValue();
        } else if (val instanceof GeoPointData) {
            // we have no access fns that interact with double[4] arrays (the getValue() data type)...
            return val.getDisplayText();
        } else if (val instanceof GeoShapeData) {
            // we have no access fns that interact with GeoShape objects (the getValue() data type)...
            return val.getDisplayText();
        } else if (val instanceof GeoTraceData) {
            // we have no access fns that interact with GeoTrace objects (the getValue() data type)...
            return val.getDisplayText();
        } else {
            logger.warn("unrecognized data type in xpath expr: " + val.getClass().getName());
            return val.getValue(); //is this a good idea?
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{path-expr:");
        switch (init_context) {
            case INIT_CONTEXT_ROOT:
                sb.append("abs");
                break;
            case INIT_CONTEXT_RELATIVE:
                sb.append("rel");
                break;
            case INIT_CONTEXT_EXPR:
                sb.append(filtExpr.toString());
                break;
        }
        sb.append(",{");
        for (int i = 0; i < steps.length; i++) {
            sb.append(steps[i].toString());
            if (i < steps.length - 1)
                sb.append(",");
        }
        sb.append("}}");

        return sb.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof XPathPathExpr) {
            XPathPathExpr x = (XPathPathExpr) o;

            //Shortcuts for easily comparable values
            if (init_context != x.init_context || steps.length != x.steps.length) {
                return false;
            }

            return ExtUtil.arrayEquals(steps, x.steps) && (init_context != INIT_CONTEXT_EXPR || filtExpr.equals(x.filtExpr));
        } else {
            return false;
        }
    }

    /**
     * Warning: this method has somewhat unclear semantics.
     *
     * "matches" follows roughly the same process as equals(), in that it goes
     * through the path step by step and compares whether each step can refer to the same node.
     * The only difference is that match() will allow for a named step to match a step who's name
     * is a wildcard.
     *
     * So
     * \/data\/path\/to
     * will "match"
     * \/data\/*\/to
     * <p>
     * even though they are not equal.
     * <p>
     * Matching is reflexive, consistent, and symmetric, but _not_ transitive.
     *
     * @param o
     * @return true if the expression is a path that matches this one
     */
    public boolean matches(XPathExpression o) {
        if (o instanceof XPathPathExpr) {
            XPathPathExpr x = (XPathPathExpr) o;

            //Shortcuts for easily comparable values
            if (init_context != x.init_context || steps.length != x.steps.length) {
                return false;
            }

            if (steps.length != x.steps.length) {
                return false;
            } else {
                for (int i = 0; i < steps.length; i++) {
                    if (!steps[i].matches(x.steps[i])) {
                        return false;
                    }
                }
            }

            // If all steps match, we still need to make sure we're in the same "context" if this
            // is a normal expression.
            return (init_context == INIT_CONTEXT_EXPR ? filtExpr.equals(x.filtExpr) : true);
        } else {
            return false;
        }
    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        init_context = ExtUtil.readInt(in);
        if (init_context == INIT_CONTEXT_EXPR) {
            filtExpr = (XPathFilterExpr) ExtUtil.read(in, XPathFilterExpr.class, pf);
        }

        List<Object> v = (List<Object>) ExtUtil.read(in, new ExtWrapList(XPathStep.class), pf);
        steps = new XPathStep[v.size()];
        for (int i = 0; i < steps.length; i++)
            steps[i] = ((XPathStep) v.get(i)).intern();
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeNumeric(out, init_context);
        if (init_context == INIT_CONTEXT_EXPR) {
            ExtUtil.write(out, filtExpr);
        }

        List<XPathStep> v = Arrays.asList(steps);
        ExtUtil.write(out, new ExtWrapList(v));
    }

    public static XPathPathExpr fromRef(TreeReference ref) {
        XPathPathExpr path = new XPathPathExpr();
        path.init_context = (ref.isAbsolute() ? INIT_CONTEXT_ROOT : INIT_CONTEXT_RELATIVE);
        path.steps = new XPathStep[ref.size()];
        for (int i = 0; i < path.steps.length; i++) {
            if (ref.getName(i).equals(TreeReference.NAME_WILDCARD)) {
                path.steps[i] = new XPathStep(XPathStep.AXIS_CHILD, XPathStep.TEST_NAME_WILDCARD).intern();
            } else {
                path.steps[i] = new XPathStep(XPathStep.AXIS_CHILD, new XPathQName(ref.getName(i))).intern();
            }
        }
        return path;
    }

    @Override
    public Object pivot(DataInstance model, EvaluationContext evalContext, List<Object> pivots, Object sentinal) throws UnpivotableExpressionException {
        TreeReference ref = getReference();
        //Either concretely the sentinal, or "."
        if (ref.equals(sentinal) || (ref.getRefLevel() == 0)) {
            return sentinal;
        }
        //It's very, very hard to figure out how to pivot predicates. For now, just skip it
        for (int i = 0; i < ref.size(); ++i) {
            if (ref.getPredicate(i) != null && ref.getPredicate(i).size() > 0) {
                throw new UnpivotableExpressionException("Can't pivot filtered treereferences. Ref: " + ref.toString(true) + " has predicates.");
            }
        }
        return eval(model, evalContext);
    }
}