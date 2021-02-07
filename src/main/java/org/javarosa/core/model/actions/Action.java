/**
 * 
 */
package org.javarosa.core.model.actions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * @author ctsims
 *
 */
public abstract class Action implements Externalizable {
    private String name;

    public Action() {
        // for serialization
    }

    public Action(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
}
