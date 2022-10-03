package org.javarosa.core.util.externalizable;

import org.javarosa.core.util.Extras;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;

public class ExternalizableExtras extends Extras<Externalizable> implements Externalizable {

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        HashMap<String, Externalizable> extras = (HashMap<String, Externalizable>) ExtUtil.read(in, new ExtWrapMap(String.class, new ExtWrapExternalizable()), pf);
        extras.entrySet().stream().forEach(entry -> put(entry.getValue()));
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        HashMap<Object, Object> wrappedParseAttachments = new HashMap<>();
        map.entrySet().stream().forEach(entry -> {
            wrappedParseAttachments.put(entry.getKey(), new ExtWrapExternalizable(entry.getValue()));
        });

        ExtUtil.write(out, new ExtWrapMap(wrappedParseAttachments));
    }
}
