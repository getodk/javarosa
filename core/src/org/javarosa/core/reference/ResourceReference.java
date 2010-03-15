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
public class ResourceReference implements Reference {
	
	String URI;
	
	public ResourceReference(String URI) {
		this.URI = URI;
	}
	
	public boolean doesBinaryExist() {
		InputStream is = System.class.getResourceAsStream(URI);
		if(is == null) {
			return false;
		} else {
			try {
				is.close();
			} catch(IOException e) {
				//TODO: Honestly, I dunno what to do about this, it happens
				//sometimes...
				e.printStackTrace();
			}
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
}
