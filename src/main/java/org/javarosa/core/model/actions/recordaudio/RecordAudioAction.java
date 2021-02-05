package org.javarosa.core.model.actions.recordaudio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.actions.Action;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class RecordAudioAction extends Action {
    private TreeReference targetReference;
    private String quality;

    public RecordAudioAction(TreeReference targetReference, String quality) {
        super(RecordAudioActionHandler.ELEMENT_NAME);
        this.targetReference = targetReference;
        this.quality = quality;
    }

    public RecordAudioAction() {
        // empty body for serialization
    }

    @Override
    public TreeReference processAction(FormDef model, TreeReference contextRef) {
        TreeReference contextualizedTargetReference = contextRef == null ? this.targetReference
            : this.targetReference.contextualize(contextRef);

        if (RecordAudioActions.getRecordAudioListener() != null) {
            RecordAudioActions.getRecordAudioListener().recordAudioTriggered(contextualizedTargetReference, quality);
        }

        return contextualizedTargetReference;
    }

    //region serialization
    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);

        targetReference = (TreeReference) ExtUtil.read(in, new ExtWrapNullable(TreeReference.class), pf);
        quality = (String) ExtUtil.read(in, new ExtWrapNullable(String.class), pf);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);

        ExtUtil.write(out, new ExtWrapNullable(targetReference));
        ExtUtil.write(out, new ExtWrapNullable(quality));
    }
    //endregion
}
