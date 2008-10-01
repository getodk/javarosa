package org.javarosa.core.util;

import java.io.DataInputStream;
import java.io.IOException;

import org.javarosa.core.util.externalizable.PrototypeFactory;

public interface ExternalizableDynamic extends Externalizable {

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException;
}
