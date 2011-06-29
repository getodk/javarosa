/**
 * 
 */
package org.javarosa.j2me.file;

import org.javarosa.core.reference.PrefixedRootFactory;
import org.javarosa.core.reference.Reference;


/**
 * A J2meFileRoot profiles a mechanism for deriving roots
 * of the type "jr://file" in the context of a J2ME runtime
 * environment. It is created with a localRoot part which
 * defines what local file root should be used in the 
 * current environment.
 * 
 * @author ctsims
 *
 */
public class J2meFileRoot extends PrefixedRootFactory {
	
	private String localRoot;
	
	/**
	 * Creates a RootFactory which derives file roots
	 * in the local environment to the localRoot provided
	 * 
	 * @param localRoot A local file root in the current
	 * runtime environment.
	 */
	public J2meFileRoot(String localRoot) {
		super(new String[] {"file"});
		this.localRoot = localRoot;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.reference.PrefixedRootFactory#factory(java.lang.String, java.lang.String)
	 */
	protected Reference factory(String terminal, String URI) {
		return new J2meFileReference(localRoot,  terminal);
	}
}
