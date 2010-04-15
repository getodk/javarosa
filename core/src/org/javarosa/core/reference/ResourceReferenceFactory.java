/**
 * 
 */
package org.javarosa.core.reference;

/**
 * A ResourceReferenceFactory is a Raw Reference Accessor
 * which provides a factory for references of the form
 * <pre>jr://resource/</pre>.
 * 
 * TODO: Configure this factory to also work for raw resource
 * accessors like "/something".
 * 
 * @author ctsims
 *
 */
public class ResourceReferenceFactory extends PrefixedRootFactory {

	public ResourceReferenceFactory() {
		super(new String[] {"resource"});
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.PrefixedRootFactory#factory(java.lang.String, java.lang.String)
	 */
	protected Reference factory(String terminal, String URI) {
		return new ResourceReference(terminal);
	}
}
