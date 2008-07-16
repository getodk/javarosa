/**
 * 
 */
package org.javarosa.model.xform;

import org.javarosa.core.model.IDataReference;

/**
 * 
 */
public class XPathReference implements IDataReference {
	private String xpath;
	
	public XPathReference () {
		this(null);
	}
	
	public XPathReference (String xpath) {
		setReference(xpath);
	}
	
	public Object getReference () {
		return xpath;
	}
	
	public void setReference (Object o) {
		xpath = (String)o;
	}
}
