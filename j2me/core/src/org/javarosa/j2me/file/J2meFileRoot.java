/**
 * 
 */
package org.javarosa.j2me.file;

import org.javarosa.core.reference.PrefixedRootFactory;
import org.javarosa.core.reference.Reference;


/**
 * @author ctsims
 *
 */
public class J2meFileRoot extends PrefixedRootFactory {
	
	private String localRoot;
	
	public J2meFileRoot(String localRoot) {
		super(new String[] {"file"});
		this.localRoot = localRoot;
	}

	protected Reference factory(String terminal, String URI) {
		return new J2meFileReference(localRoot,  terminal);
	}
}
