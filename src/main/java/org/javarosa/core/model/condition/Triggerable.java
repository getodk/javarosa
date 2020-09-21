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
    private Set<TreeReference> targets;

    /**
     * Current reference which is the "Basis" of the trigerrables being evaluated. This is the highest
     * common root of all of the targets being evaluated.
     */
    private TreeReference contextRef;  //generic ref used to turn triggers into absolute references

    /**
     * The first context provided to this triggerable before reducing to the common root (see Triggerable.intersectContextWith).
     * This is just the first bind encountered with the expression represented by this Triggerable so it's not clear how
     * it's useful (and bind order for the same form definition affects its value).
     */
    private TreeReference originalContextRef;

    // TODO Move this into the DAG. This shouldn't be here.
    private Set<QuickTriggerable> immediateCascades = null;

    Triggerable() {

    }

    Triggerable(XPathConditional expr, TreeReference contextRef, TreeReference originalContextRef, Set<TreeReference> targets, Set<QuickTriggerable> immediateCascades) {
        this.expr = expr;
        this.targets = targets;
        this.contextRef = contextRef;
        this.originalContextRef = originalContextRef;
        this.immediateCascades = immediateCascades;
    }

    public static Triggerable condition(XPathConditional expr, ConditionAction trueAction, ConditionAction falseAction, TreeReference contextRef) {
        return new Condition(expr, contextRef, contextRef, new HashSet<>(), new HashSet<>(), trueAction, falseAction);
    }

    public static Triggerable recalculate(XPathConditional expr, TreeReference contextRef) {
        return new Recalculate(expr, contextRef, contextRef, new HashSet<>(), new HashSet<>());
    }

    public abstract Object eval(FormInstance instance, EvaluationContext ec);

    protected abstract void apply(TreeReference ref, Object result, FormInstance mainInstance);

    public abstract boolean canCascade();

    public abstract boolean isCascadingToChildren();

    public Set<TreeReference> getTriggers() {
        return expr.getTriggers(originalContextRef);
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

    public Set<TreeReference> getTargets() {
        return targets;
    }

    public void intersectContextWith(Triggerable other) {
        contextRef = contextRef.intersect(other.contextRef);
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
        targets.add(target);
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

    String buildHumanReadableTargetList() {
        StringBuilder targetsBuilder = new StringBuilder();
        for (TreeReference t : getTargets())
            targetsBuilder.append(t.toString(true, true)).append(", ");
        String targetsString = targetsBuilder.toString();
        return targetsString.isEmpty()
            ? "unknown refs (no targets added yet)"
            : targetsString.substring(0, targetsString.length() - 2);
    }

    // region External serialization

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        expr = (XPathConditional) ExtUtil.read(in, new ExtWrapTagged(), pf);
        contextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        originalContextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        targets = new HashSet<>((List<TreeReference>) ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(expr));
        ExtUtil.write(out, contextRef);
        ExtUtil.write(out, originalContextRef);
        ExtUtil.write(out, new ExtWrapList(new ArrayList<>(targets)));
    }

    // endregion
}
