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

package org.javarosa.core.model.condition;

import static org.javarosa.core.model.DataType.BOOLEAN;
import static org.javarosa.core.model.DataType.CHOICE;
import static org.javarosa.core.model.DataType.DATE;
import static org.javarosa.core.model.DataType.GEOPOINT;
import static org.javarosa.core.model.DataType.GEOSHAPE;
import static org.javarosa.core.model.DataType.GEOTRACE;
import static org.javarosa.core.model.DataType.INTEGER;
import static org.javarosa.core.model.DataType.LONG;
import static org.javarosa.core.model.DataType.MULTIPLE_ITEMS;
import static org.javarosa.core.model.DataType.TIME;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javarosa.core.model.DataType;
import org.javarosa.core.model.QuickTriggerable;
import org.javarosa.core.model.data.BooleanData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.DateTimeData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.GeoShapeData;
import org.javarosa.core.model.data.GeoTraceData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.MultipleItemsData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.xpath.XPathException;

public class Recalculate extends Triggerable {

    /**
     * Constructor required for deserialization
     */
    @SuppressWarnings("unused")
    public Recalculate() {

    }

    protected Recalculate(IConditionExpr expr, TreeReference contextRef, TreeReference originalContextRef, List<TreeReference> targets, Set<QuickTriggerable> immediateCascades) {
        super(expr, contextRef, originalContextRef, targets, immediateCascades);
    }

    @Override
    public Object eval(FormInstance model, EvaluationContext ec) {
        return evalRaw(model, ec);
    }

    @Override
    public void apply(TreeReference ref, Object result, FormInstance mainInstance) {
        TreeElement element = mainInstance.resolveReference(ref);
        int dataType = element.getDataType();
        element.setAnswer(wrapData(result, dataType));
    }

    @Override
    public boolean canCascade() {
        return true;
    }

    // TODO Improve this method and simplify
    @Override
    public boolean equals(Object o) {
        if (o instanceof Recalculate) {
            Recalculate r = (Recalculate) o;
            boolean result = false;
            if (r instanceof Triggerable) {
                Triggerable t = (Triggerable) r;
                if (this == t) {
                    result = true;
                } else if (this.expr.equals(t.expr)) {

                    // The original logic did not make any sense --
                    // the
                    try {
                        // resolved triggers should match...
                        Set<TreeReference> Atriggers = this.getTriggers();
                        Set<TreeReference> Btriggers = t.getTriggers();

                        result = (Atriggers.size() == Btriggers.size()) &&
                            Atriggers.containsAll(Btriggers);
                    } catch (XPathException e) {
                    }
                }

            }
            return this == r || result;

        } else {
            return false;
        }
    }

    //droos 1/29/10: we need to come up with a consistent rule for whether the resulting data is determined
    //by the type of the instance node, or the type of the expression result. right now it's a mix and a mess
    //note a caveat with going solely by instance node type is that untyped nodes default to string!

    //for now, these are the rules:
    // if node type == bool, convert to boolean (for numbers, zero = f, non-zero = t; empty string = f, all other datatypes -> error)
    // if numeric data, convert to int if node type is int OR data is an integer; else convert to double
    // if string data or date data, keep as is
    // if NaN or empty string, null

    /**
     * convert the data object returned by the xpath expression into an IAnswerData suitable for
     * storage in the FormInstance
     */
    // TODO Move this method into IAnswerData
    public static IAnswerData wrapData(Object val, int intDataType) {
        if ((val instanceof String && ((String) val).length() == 0) ||
            (val instanceof Double && ((Double) val).isNaN())) {
            return null;
        }

        final DataType dataType = DataType.from(intDataType);

        if (BOOLEAN == dataType || val instanceof Boolean) {
            //ctsims: We should really be using the boolean datatype for real, it's
            //necessary for backend calculations and XSD compliance

            boolean b;

            if (val instanceof Boolean) {
                b = (Boolean) val;
            } else if (val instanceof Double) {
                double d = (Double) val;
                b = Math.abs(d) > 1.0e-12 && !Double.isNaN(d);
            } else if (val instanceof String) {
                String s = (String) val;
                b = s.length() > 0;
            } else {
                throw new RuntimeException("unrecognized data representation while trying to convert to BOOLEAN");
            }

            return new BooleanData(b);
        } else if (val instanceof Double) {
            double d = (Double) val;
            long l = (long) d;
            boolean isIntegral = Math.abs(d - l) < 1.0e-9;
            if (INTEGER == dataType ||
                (isIntegral && (Integer.MAX_VALUE >= l) && (Integer.MIN_VALUE <= l))) {
                return new IntegerData((int) d);
            } else if (LONG == dataType || isIntegral) {
                return new LongData((long) d);
            } else {
                return new DecimalData(d);
            }
        } else if (dataType == GEOPOINT) {
            return new GeoPointData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == GEOSHAPE) {
            return new GeoShapeData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == GEOTRACE) {
            return new GeoTraceData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == CHOICE) {
            return new SelectOneData().cast(new UncastData(String.valueOf(val)));
        } else if (dataType == MULTIPLE_ITEMS) {
            return new MultipleItemsData().cast(new UncastData(String.valueOf(val)));
        } else if (val instanceof String) {
            return new StringData((String) val);
        } else if (val instanceof Date) {
            if (dataType == TIME)
                return new TimeData((Date) val);
            if (dataType == DATE)
                return new DateData((Date) val);
            return new DateTimeData((Date) val);
        } else {
            throw new RuntimeException("unrecognized data type in 'calculate' expression: " + val.getClass().getName());
        }
    }

    @Override
    public void setImmediateCascades(Set<QuickTriggerable> cascades) {
        immediateCascades = new HashSet<>(cascades);
    }

    @Override
    public Set<QuickTriggerable> getImmediateCascades() {
        return immediateCascades;
    }

    @Override
    public TreeReference getContext() {
        return contextRef;
    }

    @Override
    public TreeReference getOriginalContext() {
        return originalContextRef;
    }

    /**
     * Dispatches all of the evaluation
     */
    @Override
    public final List<EvaluationResult> apply(FormInstance mainInstance, EvaluationContext parentContext, TreeReference context) {
        //The triggeringRoot is the highest level of actual data we can inquire about, but it _isn't_ necessarily the basis
        //for the actual expressions, so we need genericize that ref against the current context
        TreeReference ungenericised = originalContextRef.contextualize(context);
        EvaluationContext ec = new EvaluationContext(parentContext, ungenericised);

        Object result = eval(mainInstance, ec);

        List<EvaluationResult> affectedNodes = new ArrayList<>(0);
        for (TreeReference target : targets) {
            TreeReference targetRef = target.contextualize(ec.getContextRef());
            List<TreeReference> v = ec.expandReference(targetRef);

            for (TreeReference affectedRef : v) {
                apply(affectedRef, result, mainInstance);

                affectedNodes.add(new EvaluationResult(affectedRef, result));
            }
        }

        return affectedNodes;
    }

    public IConditionExpr getExpr() {
        return expr;
    }

    @Override
    public void addTarget(TreeReference target) {
        if (targets.indexOf(target) == -1) {
            targets.add(target);
        }
    }

    @Override
    public List<TreeReference> getTargets() {
        return targets;
    }

    public Set<TreeReference> getTriggers() {
        Set<TreeReference> relTriggers = expr.getTriggers(null);  /// should this be originalContextRef???
        Set<TreeReference> absTriggers = new HashSet<>();
        for (TreeReference r : relTriggers) {
            absTriggers.add(r.anchor(originalContextRef));
        }
        return absTriggers;
    }

    Object evalRaw(FormInstance model, EvaluationContext evalContext) {
        try {
            return expr.evalRaw(model, evalContext);
        } catch (XPathException e) {
            e.setSource("calculate expression for " + contextRef.toString(true));
            throw e;
        }
    }

    @Override
    public void changeContextRefToIntersectWithTriggerable(Triggerable t) {
        contextRef = contextRef.intersect(t.contextRef);
    }

    @Override
    public TreeReference contextualizeContextRef(TreeReference anchorRef) {
        // Contextualize the reference used by the triggerable against
        // the anchor
        return contextRef.contextualize(anchorRef);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        expr = (IConditionExpr) ExtUtil.read(in, new ExtWrapTagged(), pf);
        contextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        originalContextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        List<TreeReference> tlist = (List<TreeReference>) ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf);
        targets = new ArrayList<>(tlist);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(expr));
        ExtUtil.write(out, contextRef);
        ExtUtil.write(out, originalContextRef);
        List<TreeReference> tlist = new ArrayList<>(targets);
        ExtUtil.write(out, new ExtWrapList(tlist));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < targets.size(); i++) {
            sb.append(targets.get(i).toString());
            if (i < targets.size() - 1)
                sb.append(",");
        }
        return "trig[expr:" + expr.toString() + ";targets[" + sb.toString() + "]]";
    }

    /**
     * Searches in the triggers of this Triggerable, trying to find the ones that are
     * contained in the given list of contextualized refs.
     *
     * @param firedAnchorsMap a map of absolute refs
     * @return a list of affected nodes.
     */
    @Override
    public List<TreeReference> findAffectedTriggers(Map<TreeReference, List<TreeReference>> firedAnchorsMap) {
        List<TreeReference> affectedTriggers = new ArrayList<>(0);

        Set<TreeReference> triggers = this.getTriggers();
        for (TreeReference trigger : triggers) {
            List<TreeReference> firedAnchors = firedAnchorsMap.get(trigger.genericize());
            if (firedAnchors == null) {
                continue;
            }

            affectedTriggers.addAll(firedAnchors);
        }

        return affectedTriggers;
    }

    @Override
    public boolean isCascadingToChildren() {
        return false;
    }
}
