/**
 * 
 */
package org.javarosa.core.reference;

/**
 * @author ctsims
 *
 */
public class ResourceRoot implements RawRoot {

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.RawRoot#derive(java.lang.String)
	 */
	public Reference derive(String URI) throws InvalidReferenceException {
		String resourceURI = URI.substring("jr://resource".length());
		return new ResourceReference(resourceURI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.RawRoot#derive(java.lang.String, java.lang.String)
	 */
	public Reference derive(String URI, String context) throws InvalidReferenceException {
		String referenceURI = context.substring(0,context.lastIndexOf('/') + 1) + URI;
		return ReferenceManager._().DeriveReference(referenceURI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.RawRoot#derives(java.lang.String)
	 */
	public boolean derives(String URI) {
		if(URI.startsWith("jr://resource")) {
			return true;
		}
		return false;
	}

}
