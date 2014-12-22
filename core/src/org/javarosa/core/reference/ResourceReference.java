/**
 * 
 */
package org.javarosa.core.reference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A Resource Reference is a reference to a file which 
 * is a Java Resource, which is accessible with the
 * 'getResourceAsStream' method from the Java Classloader.
 * 
 * Resource References are read only, and can identify with
 * certainty whether a binary file is located at them. 
 * 
 * @author ctsims
 *
 */
public class ResourceReference implements Reference {
	
	String URI;
	
	/**
	 * Creates a new resource reference with URI in the format
	 * of a fully global resource URI, IE: "/path/file.ext".
	 * 
	 * @param URI
	 */
	public ResourceReference(String URI) {
		this.URI = URI;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#doesBinaryExist()
	 */
	public boolean doesBinaryExist() throws IOException {
		//Figure out if there's a file by trying to open
		//a stream to it and determining if it's null.
		InputStream is = System.class.getResourceAsStream(URI);
		if(is == null) {
			return false;
		} else {
			is.close();
			return true;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getStream()
	 */
	public InputStream getStream() throws IOException {
		InputStream is = System.class.getResourceAsStream(URI);
		return is;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getURI()
	 */
	public String getURI() {
		return "jr://" + "resource" + this.URI;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if(o instanceof ResourceReference) {
			return URI.equals(((ResourceReference)o).URI);
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#getOutputStream()
	 */
	public OutputStream getOutputStream() throws IOException {
		throw new IOException("Resource references are read-only URI's");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.Reference#remove()
 	*/
	public void remove() throws IOException {
		throw new IOException("Resource references are read-only URI's");
	}

	public String getLocalURI() {
		return URI;
	}
	
	public Reference[] probeAlternativeReferences() {
		//We can't poll the JAR for resources, unfortunately. It's possible
		//we could try to figure out something about the file and poll alternatives
		//based on type (PNG-> JPG, etc)
		return new Reference [0];
	}
}
