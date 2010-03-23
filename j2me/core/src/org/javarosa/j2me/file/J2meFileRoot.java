/**
 * 
 */
package org.javarosa.j2me.file;

import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.RawRoot;
import org.javarosa.core.reference.Reference;


/**
 * @author ctsims
 *
 */
public class J2meFileRoot implements RawRoot {
	
	
	private String localRoot;
	
	public J2meFileRoot(String localRoot) {
		this.localRoot = localRoot;
	}

	public Reference derive(String URI) throws InvalidReferenceException {
		String fileSystemURI = URI.substring("jr://file".length());
		return new J2meFileReference(localRoot,  fileSystemURI);
	}

	public Reference derive(String URI, String context) throws InvalidReferenceException {
		//Strip out the terminal, and the "jr://file" section, and add the local root reference
		String root = context.substring("jr://file".length(),context.lastIndexOf('/') + 1);
		return new J2meFileReference(localRoot, root +  URI);
	}

	public boolean derives(String URI) {
		if(URI.toLowerCase().startsWith("jr://file")) {
			return true;
		}
		return false;
	}
}
