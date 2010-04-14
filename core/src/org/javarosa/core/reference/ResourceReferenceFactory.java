/**
 * 
 */
package org.javarosa.core.reference;

/**
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
