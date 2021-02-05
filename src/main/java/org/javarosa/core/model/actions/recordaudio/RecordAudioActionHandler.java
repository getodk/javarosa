package org.javarosa.core.model.actions.recordaudio;

import static org.javarosa.xform.parse.XFormParser.EVENT_ATTR;
import static org.javarosa.xform.parse.XFormParser.getValidEventNames;

import java.util.List;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.actions.Actions;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.IElementHandler;
import org.javarosa.xform.parse.XFormParseException;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Element;

public class RecordAudioActionHandler implements IElementHandler {
    public static final String ELEMENT_NAME = "recordaudio";

    @Override
    public void handle(XFormParser p, Element e, Object parent) {
        if (!e.getNamespace().equals(XFormParser.NAMESPACE_ODK)) {
            throw new XFormParseException("recordaudio action must be in http://www.opendatakit.org/xforms namespace");
        }

        String ref = e.getAttributeValue(null, "ref");

        if (ref == null) {
            throw new XFormParseException("odk:recordaudio action must specify a ref");
        }

        IDataReference dataRef = FormDef.getAbsRef(new XPathReference(ref), TreeReference.rootRef());
        TreeReference target = FormInstance.unpackReference(dataRef);
        p.registerActionTarget(target);

        List<String> validEventNames = getValidEventNames(e.getAttributeValue(null, EVENT_ATTR));
        for (String eventName : validEventNames) {
            if (!Actions.isTopLevelEvent(eventName)) {
                throw new XFormParseException("odk:recordaudio action may only be triggered by top-level events (e.g. odk-instance-load)");
            }
        }

        RecordAudioAction action = new RecordAudioAction(target);

        // XFormParser.parseAction already ensures parent is an IFormElement so we can safely cast
        ((IFormElement) parent).getActionController().registerEventListener(validEventNames, action);
    }
}
