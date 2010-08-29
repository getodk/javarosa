package org.javarosa.j2me.storage.rms;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.javarosa.core.services.storage.Persistable;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class RawRecord implements Persistable {

	byte[] data;
	int id;
	
	public RawRecord (int id, byte[] data) {
		this.id = id;
		this.data = data;
	}
	
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		throw new RuntimeException("write-only");
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		out.write(data);
	}

	public void setID(int ID) {
		this.id = ID;
	}

	public int getID() {
		return this.id;
	}
}
