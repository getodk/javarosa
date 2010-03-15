/**
 * 
 */
package org.javarosa.j2me.reference;

import java.io.IOException;
import java.io.InputStream;

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
	public boolean doesBinaryExist() {
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
}
