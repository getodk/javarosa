/**
 * 
 */
package org.javarosa.j2me.reference;

import org.javarosa.core.reference.RawRoot;
import org.javarosa.core.reference.Reference;

/**
 * @author ctsims
 *
 */
public class HttpRoot implements RawRoot {

	/* (non-Javadoc)
	 * @see org.commcare.reference.Root#derive(java.lang.String)
	 */
	public Reference derive(String URI) {
		return new HttpReference(URI);
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Root#derive(java.lang.String, java.lang.String)
	 */
	public Reference derive(String URI, String context) {
		String root = context.substring(0,context.lastIndexOf('/') + 1);
		return new HttpReference(root + URI);
	}

	/* (non-Javadoc)
	 * @see org.commcare.reference.Root#derives(java.lang.String)
	 */
	public boolean derives(String URI) {
		if(URI.startsWith("http://") || URI.startsWith("https://")) {
			return true;
		} else {
			return false;
		}
	}

}
