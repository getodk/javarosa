package org.javarosa.core.model.actions;

import java.util.Arrays;

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

    public static final String EVENT_XFORMS_REVALIDATE = "xforms-revalidate";

    public static final String EVENT_ODK_NEW_REPEAT = "odk-new-repeat";

    public static final String EVENT_QUESTION_VALUE_CHANGED = "xforms-value-changed";
    //endregion

    private static final String[] ALL_EVENTS = new String[]{EVENT_ODK_INSTANCE_FIRST_LOAD, EVENT_ODK_INSTANCE_LOAD,
        EVENT_ODK_NEW_REPEAT, EVENT_QUESTION_VALUE_CHANGED, EVENT_XFORMS_REVALIDATE};

    private static final String[] INSTANCE_LOAD_EVENTS = new String[]{EVENT_ODK_INSTANCE_FIRST_LOAD, EVENT_ODK_INSTANCE_LOAD};

    private static final String[] TOP_LEVEL_EVENTS = new String[]{EVENT_ODK_INSTANCE_FIRST_LOAD, EVENT_ODK_INSTANCE_LOAD,
        EVENT_XFORMS_REVALIDATE};

    public static boolean isValidEvent(String actionEventAttribute) {
        return Arrays.asList(ALL_EVENTS).contains(actionEventAttribute);
    }

    public static boolean isTopLevelEvent(String actionEventAttribute) {
        return Arrays.asList(TOP_LEVEL_EVENTS).contains(actionEventAttribute);
    }

    public static boolean isInstanceLoadEvent(String actionEventAttribute) {
        return Arrays.asList(INSTANCE_LOAD_EVENTS).contains(actionEventAttribute);
    }
}
