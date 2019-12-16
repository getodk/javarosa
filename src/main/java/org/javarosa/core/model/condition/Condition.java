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
import java.util.List;
import java.util.Set;
import org.javarosa.core.model.QuickTriggerable;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathConditional;
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

    protected Condition(XPathConditional expr, TreeReference contextRef, TreeReference originalContextRef, Set<TreeReference> targets, Set<QuickTriggerable> immediateCascades, ConditionAction trueAction, ConditionAction falseAction) {
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
            case RELEVANT:
                element.setRelevant(true);
                break;
            case NOT_RELEVANT:
                element.setRelevant(false);
                break;
            case ENABLE:
                element.setEnabled(true);
                break;
            case READ_ONLY:
                element.setEnabled(false);
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Condition
            && super.equals(o)
            && trueAction == ((Condition) o).trueAction
            && falseAction == ((Condition) o).falseAction;
    }

    @Override
    public String toString() {
        return String.format("%s %s if (%s)", trueAction.getVerb(), buildHumanReadableTargetList(), expr.xpath);
    }

    // region External Serialization

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);
        trueAction = ConditionAction.from(ExtUtil.readInt(in));
        falseAction = ConditionAction.from(ExtUtil.readInt(in));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
        ExtUtil.writeNumeric(out, trueAction.getCode());
        ExtUtil.writeNumeric(out, falseAction.getCode());
    }

    // endregion
}
