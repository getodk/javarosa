/**
 * 
 */
package org.javarosa.j2me.reference;

import org.javarosa.core.reference.PrefixedRootFactory;
import org.javarosa.core.reference.Reference;

/**
 * @author ctsims
 *
 */
public class HttpRoot extends PrefixedRootFactory {

	public HttpRoot() {
		super(new String[] {"http://","https://"});
	}
	

	protected Reference factory(String terminal, String URI) {
		return new HttpReference(URI);
	}
}
