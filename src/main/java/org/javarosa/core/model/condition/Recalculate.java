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
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;
import org.javarosa.xpath.XPathConditional;
import org.javarosa.xpath.XPathException;

public class Recalculate extends Triggerable {

    /**
     * Constructor required for deserialization
     */
    @SuppressWarnings("unused")
    public Recalculate() {

    }

    protected Recalculate(XPathConditional expr, TreeReference contextRef, TreeReference originalContextRef, Set<TreeReference> targets, Set<QuickTriggerable> immediateCascades) {
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

    @Override
    public boolean equals(Object o) {
        return o instanceof Recalculate
            && super.equals(o);
    }

    @Override
    public String toString() {
        return String.format("Recalculate %s with (%s)", buildHumanReadableTargetList(), expr.xpath);
    }

    // region External Serialization

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);
    }

    // endregion
}
