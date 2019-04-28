package org.javarosa.core.model.actions.setlocation;

import org.javarosa.core.model.instance.TreeReference;

/**
 * An odk:setlocation implementation that immediately writes that no implementation is available when the action is
 * triggered.
 */
public final class StubSetLocationAction extends SetLocationAction {
    private static final String NO_IMPLEMENTATION_MESSAGE = "no client implementation";

    public StubSetLocationAction() {
        // empty body for serialization
    }

    public StubSetLocationAction(TreeReference targetReference) {
        super(targetReference);
    }

    @Override
    public void requestLocationUpdates() {
        saveLocationValue(NO_IMPLEMENTATION_MESSAGE);
    }
}
