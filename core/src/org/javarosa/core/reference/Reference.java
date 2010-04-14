/**
 * 
 */
package org.javarosa.core.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author ctsims
 *
 */
public interface Reference {
	public boolean doesBinaryExist() throws IOException;
	public InputStream getStream() throws IOException;
	public boolean isReadOnly();
	public String getURI();
	
	//Should possibly throw another type of exception here
	//for invalid reference operation (Read only)
	public OutputStream getOutputStream() throws IOException;
	public void remove() throws IOException;
}
