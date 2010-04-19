/**
 * 
 */
package org.javarosa.j2me.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import org.javarosa.core.reference.Reference;

/**
 * @author ctsims
 *
 */
public class HttpReference implements Reference {

	String URI;
	
	public HttpReference(String URI) {
		this.URI = URI;
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Reference#doesBinaryExist()
	 */
	public boolean doesBinaryExist() throws IOException {
		//Do HTTP connection stuff? Look for a 404? 
		return true;
	}
	
	public InputStream getStream() throws IOException {
		HttpConnection connection = (HttpConnection)Connector.open(URI);
		connection.setRequestMethod(HttpConnection.GET);
		return connection.openInputStream();
	}
	
	public String getURI() {
		return URI;
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Reference#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	public OutputStream getOutputStream() throws IOException {
		//TODO: Support writing here?
		throw new IOException("JavaRosa HTTP References are readonly. Please use the transport manager for this op.");
	}

	public void remove() throws IOException {
		throw new IOException("JavaRosa HTTP References are readonly. Please use the transport manager for this op.");
	}
}
