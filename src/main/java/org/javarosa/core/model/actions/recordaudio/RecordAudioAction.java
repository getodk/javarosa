package org.javarosa.core.model.actions.recordaudio;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.actions.Action;
import org.javarosa.core.model.instance.TreeReference;

public class RecordAudioAction extends Action {
    private TreeReference targetReference;

    public RecordAudioAction(TreeReference targetReference) {
        this.targetReference = targetReference;
    }

    public RecordAudioAction() {
        // empty body for serialization
    }

    @Override
    public TreeReference processAction(FormDef model, TreeReference contextRef) {
        TreeReference contextualizedTargetReference = contextRef == null ? this.targetReference
            : this.targetReference.contextualize(contextRef);

        return contextualizedTargetReference;
    }
}
