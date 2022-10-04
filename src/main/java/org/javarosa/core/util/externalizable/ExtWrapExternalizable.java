package org.javarosa.core.util.externalizable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ExtWrapExternalizable extends ExternalizableWrapper {

    public ExtWrapExternalizable() {
    }

    public ExtWrapExternalizable(Externalizable externalizable) {
        this.val = externalizable;
    }

    @Override
    public ExternalizableWrapper clone(Object val) {
        return null;
    }

    @Override
    public void metaReadExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {

    }

    @Override
    public void metaWriteExternal(DataOutputStream out) throws IOException {

    }

    @Override
    public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
        try {
            String className = ExtUtil.readString(in);
            Class<?> clazz = Class.forName(className);

            this.val = ExtUtil.read(in, clazz);
        } catch (ClassNotFoundException e) {
            throw new DeserializationException("Couldn't find class from serialize class name!");
        }
    }

    @Override
    public void writeExternal(DataOutputStream out) throws IOException {
        ExtUtil.write(out, val.getClass().getName());
        ExtUtil.write(out, val);
    }
}
