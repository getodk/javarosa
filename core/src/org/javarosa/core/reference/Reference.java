/**
 * 
 */
package org.javarosa.core.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Reference is essentially a pointer to interact in a limited
 * fashion with an external resource of some kind (images, xforms,
 * etc). 
 * 
 * References are retrieved from the ReferenceManager, which is
 * responsible for turning different URI's (either normal http://,
 * etc URI's or JavaRosa jr:// URI's) into a referenc to an actual
 * resource.
 * 
 * @author ctsims
 *
 */
public interface Reference {
	/**
	 * @return True if the binary does (or might) exist at
	 * the remote location. False if the binary definitely
	 * does not exist.
	 * @throws IOException If there is a problem identifying
	 * the status of the resource
	 */
	public boolean doesBinaryExist() throws IOException;
	
	/**
	 * @return A Stream of data which is the binary resource's
	 * definition.
	 * 
	 * @throws IOException If there is a problem reading the
	 * stream.
	 */
	public InputStream getStream() throws IOException;
	
	
	/**
	 * @return A URI which will evaluate to this same reference
	 * in the future.
	 */
	public String getURI();
	
	/**
	 * @return True if the remote data is only available to
	 * be read from (using getStream), False if the remote
	 * data can also be modified or written to.
	 */
	public boolean isReadOnly();
	
	//Should possibly throw another type of exception here
	//for invalid reference operation (Read only)
	/**
	 * @return A stream which can be written to at the
	 * reference location to define the binary content there.
	 * 
	 * @throws IOException If there is a problem writing or the
	 * reference is read only
	 */
	public OutputStream getOutputStream() throws IOException;
	
	/**
	 * Removes the binary data located by this reference.
	 * @throws IOException If there is a problem deleting or the
	 * reference is read only
	 */
	public void remove() throws IOException;
}
