/**
 * 
 */
package org.javarosa.core.reference;

/**
 * PrefixedRootFactory provides a clean way to implement
 * the vast majority of behavior for a reference factory.
 * 
 * A PrefixedRootFactory defines a set of roots which it can
 * provide references for. Roots can either be a full URI root
 * like "http://", or "file://", or can be local roots like
 * "resource" or "file", in which case the assumed protocol
 * parent will be "jr://". 
 * 
 * For example: a PrefixedRootFactory with the roots "media"
 * and "resource" will be used by the ReferenceManager to derive
 * any URI with the roots
 * <ul><li>jr://media/</li><li>jr://resource/</li></ul> like 
 * <pre>jr://media/checkmark.png</pre>
 * and a PrefixedRootFactory with roots "file" and "file://" will
 * be used by the ReferenceManager to derive any URI with the roots
 * <ul><li>jr://file/</li><li>file://</li></ul> like 
 * <pre>jr://file/myxform.xhtml</pre> or <pre>file://myxform.xhtml</pre>
 * 
 * @author ctsims
 *
 */
public abstract class PrefixedRootFactory implements ReferenceFactory {

	String[] roots;
	
	/**
	 * Construct a PrefixedRootFactory which handles the roots 
	 * provided. Implementing subclasses will actually do the
	 * root construction by implementing the "factory()" method.
	 * 
	 * @param roots The roots of URI's which should be derived by
	 * this factory.
	 */
	public PrefixedRootFactory(String[] roots) {
		this.roots = new String[roots.length];
		for(int i = 0 ; i < this.roots.length; ++i) {
			if(roots[i].indexOf("://") != -1) {
				//If this is the kind of root which is handling non-jr
				//stuff, we should leave it alone.
				this.roots[i] = roots[i];
			} else {
				//Otherwise it's just floating, so we should clearly
				//append our jr root for now.
				this.roots[i] = "jr://" + roots[i];
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.ReferenceFactory#derive(java.lang.String)
	 */
	public Reference derive(String URI) throws InvalidReferenceException {
		for(String root : roots) {
			if(URI.indexOf(root) != -1) {
				return factory(URI.substring(root.length()),URI);
			}
		}
		throw new InvalidReferenceException("Invalid attempt to derive a reference from a prefixed root. Valid prefixes for this factory are " + roots,URI);
	}
	
	/**
	 * Creates a Reference using the most locally available part of a 
	 * URI. 
	 * 
	 * @param terminal The local part of the URI for this prefixed root
	 * (excluding the root itself)
	 * @param URI The full URI 
	 * @return A reference which describes the URI 
	 */
	protected abstract Reference factory(String terminal, String URI);

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.ReferenceFactory#derive(java.lang.String, java.lang.String)
	 */
	public Reference derive(String URI, String context) throws InvalidReferenceException {
		String referenceURI = context.substring(0,context.lastIndexOf('/') + 1) + URI;
		return ReferenceManager._().DeriveReference(referenceURI);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.reference.ReferenceFactory#derives(java.lang.String)
	 */
	public boolean derives(String URI) {
		for(String root : roots) {
			if(URI.indexOf(root) != -1) {
				return true;
			}
		}
		return false;
	}

}
