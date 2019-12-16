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
import org.javarosa.core.model.data.IAnswerData;
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
        try {
            return expr.evalRaw(model, ec);
        } catch (XPathException e) {
            e.setSource("Calculate expression for " + contextRef.toString(true));
            throw e;
        }
    }

    @Override
    public void apply(TreeReference ref, Object result, FormInstance mainInstance) {
        TreeElement element = mainInstance.resolveReference(ref);
        element.setAnswer(IAnswerData.wrapData(result, element.getDataType()));
    }

    @Override
    public boolean canCascade() {
        return true;
    }

    @Override
    public boolean isCascadingToChildren() {
        return false;
    }

    // TODO Improve this method and simplify
    @Override
    public boolean equals(Object o) {
        if (o instanceof Recalculate) {
            Recalculate r = (Recalculate) o;
            boolean result = false;
            if (r instanceof Triggerable) {
                Triggerable t = r;
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
            return this == r || result;

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
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, new ExtWrapTagged(getExpr()));
        ExtUtil.write(out, contextRef);
        ExtUtil.write(out, originalContextRef);
        List<TreeReference> tlist = new ArrayList<>(targets);
        ExtUtil.write(out, new ExtWrapList(tlist));
    }

    // endregion

}
