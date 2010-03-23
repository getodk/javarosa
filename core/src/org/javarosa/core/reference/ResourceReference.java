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
public class ResourceReference implements Reference {
	
	String URI;
	
	public ResourceReference(String URI) {
		this.URI = URI;
	}
	
	public boolean doesBinaryExist() throws IOException {
		InputStream is = System.class.getResourceAsStream(URI);
		if(is == null) {
			return false;
		} else {
			is.close();
			return true;
		}
	}
	
	public InputStream getStream() throws IOException {
		InputStream is = System.class.getResourceAsStream(URI);
		return is;
	}

	public String getURI() {
		return "jr://" + "resource" + this.URI;
	}

	public boolean isReadOnly() {
		return true;
	}
	
	public boolean equals(Object o) {
		if(o instanceof ResourceReference) {
			return URI.equals(((ResourceReference)o).URI);
		} else {
			return false;
		}
	}

	public OutputStream getOutputStream() throws IOException {
		throw new IOException("Resource references are read-only URI's");
	}

	public void remove() throws IOException {
		throw new IOException("Resource references are read-only URI's");
	}
}
