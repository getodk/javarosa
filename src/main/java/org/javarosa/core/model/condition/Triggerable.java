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
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.debug.EvaluationResult;
import org.javarosa.xpath.XPathConditional;

/**
 * A triggerable represents an action that should be processed based
 * on a value updating in a model. Trigerrables are comprised of two
 * basic components: An expression to be evaluated, and a reference
 * which represents where the resultant value will be stored.
 * <p>
 * A triggerable will dispatch the action it's performing out to
 * all relevant nodes referenced by the context against thes current
 * models.
 *
 * @author ctsims
 */
public abstract class Triggerable implements Externalizable {
    /**
     * The expression which will be evaluated to produce a result
     */
    protected XPathConditional expr;

    /**
     * References to all of the (non-contextualized) nodes which should be
     * updated by the result of this triggerable
     */
    protected List<TreeReference> targets;

    /**
     * Current reference which is the "Basis" of the trigerrables being evaluated. This is the highest
     * common root of all of the targets being evaluated.
     */
    protected TreeReference contextRef;  //generic ref used to turn triggers into absolute references

    // TODO Study why we really need this property. Looking at mutators, it should always equal the contextRef.
    /**
     * The first context provided to this triggerable before reducing to the common root.
     */
    protected TreeReference originalContextRef;

    protected Set<QuickTriggerable> immediateCascades = null;

    protected Triggerable() {

    }

    protected Triggerable(XPathConditional expr, TreeReference contextRef, TreeReference originalContextRef, List<TreeReference> targets, Set<QuickTriggerable> immediateCascades) {
        this.expr = expr;
        this.targets = targets;
        this.contextRef = contextRef;
        this.originalContextRef = originalContextRef;
        this.immediateCascades = immediateCascades;
    }

    public static Triggerable condition(XPathConditional expr, ConditionAction trueAction, ConditionAction falseAction, TreeReference contextRef) {
        return new Condition(expr, contextRef, contextRef, new ArrayList<>(), new HashSet<>(), trueAction, falseAction);
    }

    public static Triggerable recalculate(XPathConditional expr, TreeReference contextRef) {
        return new Recalculate(expr, contextRef, contextRef, new ArrayList<>(), new HashSet<>());
    }

    @SuppressWarnings("unchecked")
    public static List<Triggerable> readExternalConditions(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
        return (List<Triggerable>) ExtUtil.read(dis, new ExtWrapList(Condition.class), pf);
    }

    @SuppressWarnings("unchecked")
    public static List<Triggerable> readExternalRecalculates(DataInputStream dis, PrototypeFactory pf) throws IOException, DeserializationException {
        return (List<Triggerable>) ExtUtil.read(dis, new ExtWrapList(Recalculate.class), pf);
    }

    public abstract Object eval(FormInstance instance, EvaluationContext ec);

    protected abstract void apply(TreeReference ref, Object result, FormInstance mainInstance);

    public abstract boolean canCascade();

    public abstract boolean isCascadingToChildren();

    public Set<TreeReference> getTriggers() {
        Set<TreeReference> relTriggers = expr.getTriggers(null);  /// should this be originalContextRef???
        Set<TreeReference> absTriggers = new HashSet<>();
        for (TreeReference r : relTriggers) {
            absTriggers.add(r.anchor(originalContextRef));
        }
        return absTriggers;
    }

    /**
     * Searches in the triggers of this Triggerable, trying to find the ones that are
     * contained in the given list of contextualized refs.
     *
     * @param firedAnchorsMap a map of absolute refs
     * @return a list of affected nodes.
     */
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

    public TreeReference contextualizeContextRef(TreeReference anchorRef) {
        // Contextualize the reference used by the triggerable against
        // the anchor
        return contextRef.contextualize(anchorRef);
    }

    /**
     * Dispatches all of the evaluation
     */
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

    public List<TreeReference> getTargets() {
        return targets;
    }

    public void changeContextRefToIntersectWithTriggerable(Triggerable t) {
        contextRef = contextRef.intersect(t.contextRef);
    }

    public TreeReference getContext() {
        return contextRef;
    }

    public TreeReference getOriginalContext() {
        return originalContextRef;
    }

    public void setImmediateCascades(Set<QuickTriggerable> cascades) {
        immediateCascades = new HashSet<>(cascades);
    }

    public Set<QuickTriggerable> getImmediateCascades() {
        return immediateCascades;
    }

    public IConditionExpr getExpr() {
        return expr;
    }

    public void addTarget(TreeReference target) {
        if (targets.indexOf(target) == -1) {
            targets.add(target);
        }
    }

    @Override
    public String toString() {
        StringBuilder targetsBuilder = new StringBuilder();
        for (TreeReference t : targets)
            targetsBuilder.append(t.toString(true, true)).append(", ");
        String targets = targetsBuilder.toString();
        String prettyTargets = targets.isEmpty()
            ? "unknown refs (no targets added yet)"
            : targets.substring(0, targets.length() - 2);
        return String.format("\"%s\" into %s", expr.xpath, prettyTargets);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof Triggerable))
            return false;

        Triggerable other = (Triggerable) o;

        // Both must have the same expression
        if (!expr.equals(other.expr))
            return false;

        // Both must have the same set of triggers
        if (getTriggers().size() != other.getTriggers().size())
            return false;
        if (!getTriggers().containsAll(other.getTriggers()))
            return false;

        return true;
    }

    // region External serialization

    @SuppressWarnings("unchecked")
    public static void readExternal(Triggerable t, DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        t.expr = (XPathConditional) ExtUtil.read(in, new ExtWrapTagged(), pf);
        t.contextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        t.originalContextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        t.targets = new ArrayList<>((List<TreeReference>) ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf));
    }

    public void writeExternal(Triggerable t, DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(t.expr));
        ExtUtil.write(out, t.contextRef);
        ExtUtil.write(out, t.originalContextRef);
        ExtUtil.write(out, new ExtWrapList(new ArrayList<>(t.targets)));
    }

    // endregion
}
