/**
 * 
 */
package org.javarosa.core.reference;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author ctsims
 *
 */
public interface Reference {
	public boolean doesBinaryExist();
	public InputStream getStream() throws IOException;
	public boolean isReadOnly();
	public String getURI();
}
