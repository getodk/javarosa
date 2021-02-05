package org.javarosa.core.model.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActionHandler;
import org.javarosa.core.model.actions.recordaudio.XFormsActionListener;

public class Actions {
    private Actions() { }

    //region Event names
    /**
     * Dispatched the first time a form instance is loaded.
     */
    public static final String EVENT_ODK_INSTANCE_FIRST_LOAD = "odk-instance-first-load";

    /**
     * Dispatched any time a form instance is loaded.
     */
    public static final String EVENT_ODK_INSTANCE_LOAD = "odk-instance-load";

    /**
     * @deprecated because as W3C XForms defines it, it should be dispatched any time the XForms engine is ready. In
     * JavaRosa, it was dispatched only on first load of a form instance. Use
     * {@link #EVENT_ODK_INSTANCE_FIRST_LOAD} instead.
     */
    @Deprecated
    public static final String EVENT_XFORMS_READY = "xforms-ready";
    public static final String EVENT_XFORMS_REVALIDATE = "xforms-revalidate";

    public static final String EVENT_ODK_NEW_REPEAT = "odk-new-repeat";

    /**
     * @deprecated because it was never documented. Use {@link #EVENT_ODK_NEW_REPEAT} instead.
     */
    public static final String EVENT_JR_INSERT = "jr-insert";

    public static final String EVENT_QUESTION_VALUE_CHANGED = "xforms-value-changed";
    //endregion

    private static final String[] ALL_EVENTS = new String[]{EVENT_ODK_INSTANCE_FIRST_LOAD, EVENT_ODK_INSTANCE_LOAD, EVENT_XFORMS_READY,
        EVENT_ODK_NEW_REPEAT, EVENT_JR_INSERT, EVENT_QUESTION_VALUE_CHANGED, EVENT_XFORMS_REVALIDATE};

    private static final String[] TOP_LEVEL_EVENTS = new String[]{EVENT_ODK_INSTANCE_FIRST_LOAD, EVENT_ODK_INSTANCE_LOAD, EVENT_XFORMS_READY,
        EVENT_XFORMS_REVALIDATE};

    /**
     * Global registry of client classes that want to get updates about triggered actions. Addresses the need for some
     * actions to be handled entirely client-side.
     */
    private static Map<String, XFormsActionListener> actionListeners = new HashMap<>();

    public static boolean isValidEvent(String actionEventAttribute) {
        return Arrays.asList(ALL_EVENTS).contains(actionEventAttribute);
    }

    public static boolean isTopLevelEvent(String actionEventAttribute) {
        return Arrays.asList(TOP_LEVEL_EVENTS).contains(actionEventAttribute);
    }


    public static void registerActionListener(String actionName, XFormsActionListener listener) {
        if (!actionName.equals(RecordAudioActionHandler.ELEMENT_NAME)){
            throw new IllegalArgumentException("Currently, only the recordaudio action notifies listeners");
        }

        actionListeners.put(actionName, listener);
    }

    public static void unregisterActionListener(String actionName) {
        actionListeners.remove(actionName);
    }

    public static XFormsActionListener getActionListener(String actionName) {
        return actionListeners.get(actionName);
    }

}
