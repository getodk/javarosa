/**
 * 
 */
package org.javarosa.model.xform;

import org.javarosa.core.model.IDataReference;

/**
 * 
 */
public class XPathBinding implements IDataReference {
	private String xpath;
	
	public XPathBinding () {
		this(null);
	}
	
	public XPathBinding (String xpath) {
		setReference(xpath);
	}
	
	public Object getReference () {
		return xpath;
	}
	
	public void setReference (Object o) {
		xpath = (String)o;
	}
}
