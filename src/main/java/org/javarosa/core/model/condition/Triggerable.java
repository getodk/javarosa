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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javarosa.core.model.QuickTriggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.debug.EvaluationResult;

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
    protected IConditionExpr expr;

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

    /**
     * The first context provided to this triggerable before reducing to the common root.
     */
    protected TreeReference originalContextRef;

    protected Set<QuickTriggerable> immediateCascades = null;

    public Triggerable() {

    }

    public Triggerable(IConditionExpr expr, TreeReference contextRef, ArrayList<TreeReference> targets) {
        this.expr = expr;
        this.contextRef = contextRef;
        this.originalContextRef = contextRef;
        this.targets = targets;
    }

    public Triggerable(IConditionExpr expr, TreeReference contextRef) {
        this(expr, contextRef, new ArrayList<>(0));
    }

    protected abstract Object eval(FormInstance instance, EvaluationContext ec);

    protected abstract void apply(TreeReference ref, Object result, FormInstance mainInstance);

    public abstract boolean canCascade();

    /**
     * This should return true if this triggerable's targets will implicity modify the
     * value of their children. IE: if this triggerable makes a node relevant/irrelevant,
     * expressions which care about the value of this node's children should be triggered.
     *
     * @return True if this condition should trigger expressions whose targets include
     *     nodes which are the children of this node's targets.
     */
    public abstract boolean isCascadingToChildren();

    public abstract Set<TreeReference> getTriggers();

    public abstract List<TreeReference> findAffectedTriggers(Map<TreeReference, List<TreeReference>> firedAnchors);

    public abstract TreeReference contextualizeContextRef(TreeReference anchorRef);

    public abstract List<EvaluationResult> apply(FormInstance mainInstance, EvaluationContext ec, TreeReference qualified);

    public abstract List<TreeReference> getTargets();

    public abstract void changeContextRefToIntersectWithTriggerable(Triggerable t);

    public abstract TreeReference getContext();

    public abstract TreeReference getOriginalContext();

    public abstract void setImmediateCascades(Set<QuickTriggerable> deps);

    public abstract Set<QuickTriggerable> getImmediateCascades();
}
