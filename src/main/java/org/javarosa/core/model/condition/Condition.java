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
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class Condition extends Triggerable {
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

    public Condition(IConditionExpr expr, int trueAction, int falseAction, TreeReference contextRef) {
        this(expr, trueAction, falseAction, contextRef, new ArrayList<>(0));
    }

    public Condition(IConditionExpr expr, int trueAction, int falseAction, TreeReference contextRef, ArrayList<TreeReference> targets) {
        super(expr, contextRef, targets);
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
    @Override
    public boolean equals(Object o) {
        if (o instanceof Condition) {
            Condition c = (Condition) o;
            if (this == c)
                return true;

            return (this.trueAction == c.trueAction && this.falseAction == c.falseAction && super.equals(c));
        } else {
            return false;
        }
    }

    // region External serialization

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);
        trueAction = ExtUtil.readInt(in);
        falseAction = ExtUtil.readInt(in);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
        ExtUtil.writeNumeric(out, trueAction);
        ExtUtil.writeNumeric(out, falseAction);
    }

    // endregion
}
