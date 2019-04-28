/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javarosa.core.model.actions.setlocation;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.actions.Action;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.core.model.data.AnswerDataFactory;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.AbstractTreeElement;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Abstract implementation of odk:setlocation action. Concrete implementations must:
 * - provide a way to request location updates when the action is triggered
 * - use {@link #saveLocationValue(String)} to write a location to the model
 * - provide a no-argument constructor with no body for serialization
 * - get registered by the {@link org.javarosa.core.services.PrototypeManager}
 */
public abstract class SetLocationAction extends Action {
    private TreeReference targetReference;
    private TreeReference contextualizedTargetReference;

    private FormDef formDef;

    public SetLocationAction() {
        // empty body for serialization
    }

    SetLocationAction(TreeReference targetReference) {
        super(SetLocationActionHandler.ELEMENT_NAME);
        setTargetReference(targetReference);
    }

    public TreeReference getTargetReference() {
        return targetReference;
    }

    public void setTargetReference(TreeReference targetReference) {
        this.targetReference = targetReference;
    }

    @Override
    public final TreeReference processAction(FormDef model, TreeReference contextRef) {
        this.formDef = model;
        contextualizedTargetReference = contextRef == null ? this.targetReference
            : this.targetReference.contextualize(contextRef);

        requestLocationUpdates();

        return contextualizedTargetReference;
    }

    /**
     * Client-specific location request. Implementations could immediately read a location and write it to the model if
     * available or could initiate a request and asynchronously write the location when available.
     */
    public abstract void requestLocationUpdates();

    /**
     * Save the location to the model.
     */
    public final void saveLocationValue(String location) {
        EvaluationContext context = new EvaluationContext(formDef.getEvaluationContext(), contextualizedTargetReference);
        AbstractTreeElement node = context.resolveReference(contextualizedTargetReference);
        if (node != null) {
            int dataType = node.getDataType();
            IAnswerData val = Recalculate.wrapData(location, dataType);
            if (val == null) {
                formDef.setValue(null, contextualizedTargetReference, true);
            } else {
                IAnswerData answer = AnswerDataFactory.templateByDataType(dataType).cast(val.uncast());
                formDef.setValue(answer, contextualizedTargetReference, true);
            }
        }
    }

    //region serialization
    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);

        targetReference = (TreeReference) ExtUtil.read(in, new ExtWrapNullable(TreeReference.class), pf);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);

        ExtUtil.write(out, new ExtWrapNullable(targetReference));
    }
    //endregion
}
