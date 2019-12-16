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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javarosa.core.model.QuickTriggerable;
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

public class Condition extends Triggerable {
    // TODO Create an enum to take all these constants out from this class
    public static final int ACTION_NULL = 0;
    public static final int ACTION_SHOW = 1;
    public static final int ACTION_HIDE = 2;
    public static final int ACTION_ENABLE = 3;
    public static final int ACTION_DISABLE = 4;
    public static final int ACTION_LOCK = 5;
    public static final int ACTION_UNLOCK = 6;
    public static final int ACTION_REQUIRE = 7;
    public static final int ACTION_DONT_REQUIRE = 8;

    public int trueAction;
    public int falseAction;

    /**
     * Constructor required for deserialization
     */
    @SuppressWarnings("unused")
    public Condition() {

    }

    protected Condition(IConditionExpr expr, TreeReference contextRef, TreeReference originalContextRef, List<TreeReference> targets, Set<QuickTriggerable> immediateCascades, int trueAction, int falseAction) {
        super(expr, contextRef, originalContextRef, targets, immediateCascades);
        this.trueAction = trueAction;
        this.falseAction = falseAction;
    }

    @Override
    public Object eval(FormInstance model, EvaluationContext evalContext) {
        return evalPredicate(model, evalContext);
    }

    public boolean evalBool(FormInstance model, EvaluationContext evalContext) {
        return (Boolean) eval(model, evalContext);
    }

    @Override
    public void apply(TreeReference ref, Object rawResult, FormInstance mainInstance) {
        boolean result = (Boolean) rawResult;
        performAction(mainInstance.resolveReference(ref), result ? trueAction : falseAction);
    }

    @Override
    public boolean canCascade() {
        return (trueAction == ACTION_SHOW || trueAction == ACTION_HIDE);
    }

    @Override
    public boolean isCascadingToChildren() {
        return (trueAction == ACTION_SHOW || trueAction == ACTION_HIDE);
    }


    private void performAction(TreeElement node, int action) {
        switch (action) {
            case ACTION_NULL:
                break;
            case ACTION_SHOW:
                node.setRelevant(true);
                break;
            case ACTION_HIDE:
                node.setRelevant(false);
                break;
            case ACTION_ENABLE:
                node.setEnabled(true);
                break;
            case ACTION_DISABLE:
                node.setEnabled(false);
                break;
            case ACTION_LOCK:         /* not supported */
                break;
            case ACTION_UNLOCK:       /* not supported */
                break;
            case ACTION_REQUIRE:
                node.setRequired(true);
                break;
            case ACTION_DONT_REQUIRE:
                node.setRequired(false);
                break;
        }
    }

    /**
     * conditions are equal if they have the same actions, expression, and triggers, but NOT targets or context ref
     */
    // TODO Improve this method and simplify
    @Override
    public boolean equals(Object o) {
        if (o instanceof Condition) {
            Condition c = (Condition) o;
            if (this == c)
                return true;

            boolean result = false;
            if (c instanceof Triggerable) {
                Triggerable t = (Triggerable) c;
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
            return (this.trueAction == c.trueAction && this.falseAction == c.falseAction && result);
        } else {
            return false;
        }
    }

    // region External serialization

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        expr = (IConditionExpr) ExtUtil.read(in, new ExtWrapTagged(), pf);
        contextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        originalContextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        List<TreeReference> tlist = (List<TreeReference>) ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf);
        targets = new ArrayList<>(tlist);
        trueAction = ExtUtil.readInt(in);
        falseAction = ExtUtil.readInt(in);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(expr));
        ExtUtil.write(out, contextRef);
        ExtUtil.write(out, originalContextRef);
        List<TreeReference> tlist = new ArrayList<>(targets);
        ExtUtil.write(out, new ExtWrapList(tlist));
        ExtUtil.writeNumeric(out, trueAction);
        ExtUtil.writeNumeric(out, falseAction);
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

    Boolean evalPredicate(FormInstance model, EvaluationContext evalContext) {
        try {
            return expr.eval(model, evalContext);
        } catch (XPathException e) {
            e.setSource("Relevant expression for " + contextRef.toString(true));
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

    // endregion
}
