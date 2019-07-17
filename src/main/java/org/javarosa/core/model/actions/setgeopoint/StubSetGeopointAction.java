package org.javarosa.core.model.actions.setgeopoint;

import org.javarosa.core.model.instance.TreeReference;

/**
 * An odk:setgeopoint implementation that immediately writes that no implementation is available when the action is
 * triggered.
 */
public final class StubSetGeopointAction extends SetGeopointAction {
    private static final String NO_IMPLEMENTATION_MESSAGE = "no client implementation";

    public StubSetGeopointAction() {
        // empty body for serialization
    }

    public StubSetGeopointAction(TreeReference targetReference) {
        super(targetReference);
    }

    @Override
    public void requestLocationUpdates() {
        saveLocationValue(NO_IMPLEMENTATION_MESSAGE);
    }
}
