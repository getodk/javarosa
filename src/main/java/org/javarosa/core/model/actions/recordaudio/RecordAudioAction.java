package org.javarosa.core.model.actions.recordaudio;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.actions.Action;
import org.javarosa.core.model.actions.Actions;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class RecordAudioAction extends Action {
    private TreeReference targetReference;

    public RecordAudioAction(TreeReference targetReference) {
        super(RecordAudioActionHandler.ELEMENT_NAME);
        this.targetReference = targetReference;
    }

    public RecordAudioAction() {
        // empty body for serialization
    }

    @Override
    public TreeReference processAction(FormDef model, TreeReference contextRef) {
        TreeReference contextualizedTargetReference = contextRef == null ? this.targetReference
            : this.targetReference.contextualize(contextRef);

        if (Actions.getActionListener(getName()) != null) {
            Actions.getActionListener(getName()).actionTriggered(getName(), contextualizedTargetReference);
        }

        return contextualizedTargetReference;
    }

    //region serialization
    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        super.readExternal(in, pf);

        targetReference = (TreeReference) ExtUtil.read(in, new ExtWrapNullable(TreeReference.class), pf);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        super.writeExternal(out);

        ExtUtil.write(out, new ExtWrapNullable(targetReference));
    }
    //endregion
}
