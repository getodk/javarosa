package org.javarosa.core.services.storage.utilities;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Utility class that provides methods for serialization and deserialization.
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public class Serializer {

	/**
	 * Creates a new instance of <code>Serializer</code>
	 */
	private Serializer() {
		// nothing
	}

	/**
	 * Serializes an <code>Externalizable</code> to a byte array.
	 * 
	 * @param ext
	 *            the object to serialize
	 * @return serialized object
	 * @throws IOException
	 *             if serialization fails
	 */
	public static byte[] serialize(Externalizable ext) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		ext.writeExternal(dos);
		return baos.toByteArray();
	}

	/**
	 * @param data
	 *            the data representing an serialized object
	 * @param ext
	 * @return
	 * @throws IOException
	 *             if serialization fails
	 */
	public static Externalizable deserialize(byte[] data, Externalizable ext)
			throws IOException, InstantiationException, IllegalAccessException {
		DataInputStream din = new DataInputStream(
				new ByteArrayInputStream(data));
		ext.readExternal(din);
		return ext;
	}

}
