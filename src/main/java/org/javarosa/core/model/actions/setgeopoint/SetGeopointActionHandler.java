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

package org.javarosa.core.model.actions.setgeopoint;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.IElementHandler;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Element;

public abstract class SetGeopointActionHandler implements IElementHandler {
    public static final String ELEMENT_NAME = "setgeopoint";

    @Override
    public final void handle(XFormParser p, Element e, Object parent) {
        if (!e.getNamespace().equals(XFormParser.NAMESPACE_ODK)) {
            throw new XFormParseException("setgeopoint action must be in http://www.opendatakit.org/xforms namespace");
        }

        String ref = e.getAttributeValue(null, "ref");

        if (ref == null) {
            throw new XFormParseException("odk:setgeopoint action must specify a ref");
        }

        IDataReference dataRef = FormDef.getAbsRef(new XPathReference(ref), TreeReference.rootRef());
        TreeReference target = FormInstance.unpackReference(dataRef);
        p.registerActionTarget(target);

        SetGeopointAction action = getSetGeopointAction();
        action.setTargetReference(target);

        String eventList = e.getAttributeValue(null, XFormParser.EVENT_ATTR);
        // XFormParser.parseAction already ensures parent is an IFormElement so we can safely cast
        ((IFormElement) parent).getActionController().registerEventListener(eventList, action);
    }

    /**
     * Returns an implementation for the odk:setgeopoint action.
     */
    public abstract SetGeopointAction getSetGeopointAction();
}
