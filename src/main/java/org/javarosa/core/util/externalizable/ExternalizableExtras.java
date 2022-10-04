package org.javarosa.core.util.externalizable;

import org.javarosa.core.util.Extras;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExternalizableExtras extends Extras<Externalizable> implements Externalizable {

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        ArrayList<Externalizable> extras = (ArrayList<Externalizable>) ExtUtil.read(in, new ExtWrapList(new ExtWrapExternalizable()), pf);
        extras.stream().forEach(this::put);
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        List<ExtWrapExternalizable> wrappedParseAttachments = map.values()
            .stream()
            .map(ExtWrapExternalizable::new)
            .collect(Collectors.toList());

        ExtUtil.write(out, new ExtWrapList(wrappedParseAttachments));
    }
}
