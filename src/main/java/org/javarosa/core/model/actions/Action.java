/**
 * 
 */
package org.javarosa.core.model.actions;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author ctsims
 *
 */
public abstract class Action implements Externalizable {
    // Events that can trigger an action

    /**
     * Dispatched the first time a form instance is loaded.
     */
    public static final String EVENT_ODK_INSTANCE_FIRST_LOAD = "odk-instance-first-load";

    /**
     * @deprecated because as W3C XForms defines it, it should be dispatched any time the XForms engine is ready. In
     * JavaRosa, it was dispatched only on first load of a form instance. Use
     * {@link #EVENT_ODK_INSTANCE_FIRST_LOAD} instead.
     */
    @Deprecated
    public static final String EVENT_XFORMS_READY = "xforms-ready";
    public static final String EVENT_XFORMS_REVALIDATE = "xforms-revalidate";
    public static final String EVENT_JR_INSERT = "jr-insert";
    public static final String EVENT_QUESTION_VALUE_CHANGED = "xforms-value-changed";
    private static final String[] allEvents = new String[]{EVENT_ODK_INSTANCE_FIRST_LOAD, EVENT_JR_INSERT,
                        EVENT_QUESTION_VALUE_CHANGED, EVENT_XFORMS_READY, EVENT_XFORMS_REVALIDATE};

    private String name;

    public Action() {

    }

    public Action(String name) {
        this.name = name;
    }

    /**
     * Process actions that were triggered in the form.
     *
     * NOTE: Currently actions are only processed on nodes that are
     * WITHIN the context provided, if one is provided. This will
     * need to get changed possibly for future action types.
     *
     * @return TreeReference targeted by the action or null if the action
     * wasn't completed.
     */
    public abstract TreeReference processAction(FormDef model, TreeReference context);

    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        name = ExtUtil.readString(in);
    }

    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.writeString(out,  name);
    }

    public static boolean isValidEvent(String actionEventAttribute)  {
        for (String event : allEvents) {
            if (event.equals(actionEventAttribute)) {
                return true;
            }
        }
        return false;
    }
}
