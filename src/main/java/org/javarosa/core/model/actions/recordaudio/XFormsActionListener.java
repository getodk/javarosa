package org.javarosa.core.model.actions.recordaudio;

import org.javarosa.core.model.instance.TreeReference;

public interface XFormsActionListener {
    void actionTriggered(String actionName, TreeReference absoluteTargetRef);
}