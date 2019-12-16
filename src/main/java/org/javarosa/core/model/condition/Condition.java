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
import java.util.List;
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
import org.javarosa.xpath.XPathException;

public class Condition extends Triggerable {
    public ConditionAction trueAction;
    public ConditionAction falseAction;

    /**
     * Constructor required for deserialization
     */
    @SuppressWarnings("unused")
    public Condition() {
    }

    protected Condition(IConditionExpr expr, TreeReference contextRef, TreeReference originalContextRef, List<TreeReference> targets, Set<QuickTriggerable> immediateCascades, ConditionAction trueAction, ConditionAction falseAction) {
        super(expr, contextRef, originalContextRef, targets, immediateCascades);
        this.trueAction = trueAction;
        this.falseAction = falseAction;
    }

    @Override
    public Object eval(FormInstance model, EvaluationContext evalContext) {
        try {
            return expr.eval(model, evalContext);
        } catch (XPathException e) {
            e.setSource("Condition expression for " + contextRef.toString(true));
            throw e;
        }
    }

    @Override
    public void apply(TreeReference ref, Object result, FormInstance mainInstance) {
        TreeElement element = mainInstance.resolveReference(ref);
        switch ((boolean) result ? trueAction : falseAction) {
            case NULL:
                break;
            case SHOW:
                element.setRelevant(true);
                break;
            case HIDE:
                element.setRelevant(false);
                break;
            case ENABLE:
                element.setEnabled(true);
                break;
            case DISABLE:
                element.setEnabled(false);
                break;
            case LOCK:         /* not supported */
                break;
            case UNLOCK:       /* not supported */
                break;
            case REQUIRE:
                element.setRequired(true);
                break;
            case DONT_REQUIRE:
                element.setRequired(false);
                break;
        }
    }

    @Override
    public boolean canCascade() {
        // TODO Study why we consider just the true action to decide this. Maybe we assume that if the true action is cascading, then the false action is cascading too?
        return trueAction.isCascading();
    }

    @Override
    public boolean isCascadingToChildren() {
        // TODO Study why we consider just the true action to decide this. Maybe we assume that if the true action is cascading, then the false action is cascading too?
        return trueAction.isCascading();
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
                Triggerable t = c;
                if (this == t) {
                    result = true;
                } else if (expr.equals(t.getExpr())) {

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

    @Override
    @SuppressWarnings("unchecked")
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        expr = (IConditionExpr) ExtUtil.read(in, new ExtWrapTagged(), pf);
        contextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        originalContextRef = (TreeReference) ExtUtil.read(in, TreeReference.class, pf);
        List<TreeReference> tlist = (List<TreeReference>) ExtUtil.read(in, new ExtWrapList(TreeReference.class), pf);
        targets = new ArrayList<>(tlist);
        trueAction = ConditionAction.from(ExtUtil.readInt(in));
        falseAction = ConditionAction.from(ExtUtil.readInt(in));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(getExpr()));
        ExtUtil.write(out, contextRef);
        ExtUtil.write(out, originalContextRef);
        List<TreeReference> tlist = new ArrayList<>(targets);
        ExtUtil.write(out, new ExtWrapList(tlist));
        ExtUtil.writeNumeric(out, trueAction.getCode());
        ExtUtil.writeNumeric(out, falseAction.getCode());
    }

    // endregion
}
