package org.javarosa.core.model.actions;

import org.javarosa.core.model.actions.recordaudio.XFormsActionListener;
import org.javarosa.core.model.instance.TreeReference;

public class CapturingXFormsActionListener implements XFormsActionListener {
    public String actionName;
    public TreeReference absoluteTargetRef;

    @Override
    public void actionTriggered(String actionName, TreeReference absoluteTargetRef) {
        this.actionName = actionName;
        this.absoluteTargetRef = absoluteTargetRef;
    }

    public String getActionName() {
        return actionName;
    }

    public TreeReference getAbsoluteTargetRef() {
        return absoluteTargetRef;
    }
}
