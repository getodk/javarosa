package org.javarosa.core.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

/**
 * Gives objects control over serialization. A replacement for the interfaces
 * <code>Externalizable</code> and <code>Serializable</code>, which are
 * missing in CLDC.
 * 
 * @author <a href="mailto:m.nuessler@gmail.com">Matthias Nuessler</a>
 */
public interface ExternalizableDynamic extends Externalizable {
	/**
	 * @param in
	 * @throws IOException
	 */
	public void readExternal(DataInputStream in, Vector prototypes) throws IOException, InstantiationException, IllegalAccessException, UnavailableExternalizerException;
}
