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
	public boolean referenceMatches(IDataReference reference) {
		if(reference.getClass() != XPathReference.class) {
			return getReference().equals(reference);
		}
		else {
			//Put xpath specific reference matching here;
			XPathReference extReference = (XPathReference)reference;
			//This does the same thing as above for now, eventually
			//we should fill in * references and stuff
			return extReference.xpath.equals(xpath);
		}
	}
}
