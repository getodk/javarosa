/**
 * 
 */
package org.javarosa.xform.util;

import org.javarosa.core.util.CacheTable;
import org.kxml2.io.KXmlParser;

/**
 * @author ctsims
 *
 */
public class InterningKXmlParser extends KXmlParser{
	
	CacheTable<String> stringCache;
	
	public InterningKXmlParser(CacheTable<String> stringCache) {
		super();
		this.stringCache = stringCache;
	}
	public void release() {
		//Anything?
	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getAttributeName(int)
	 */
	public String getAttributeName(int arg0) {
		return stringCache.intern(super.getAttributeName(arg0));
		
	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getAttributeNamespace(int)
	 */
	public String getAttributeNamespace(int arg0) {
		return stringCache.intern(super.getAttributeNamespace(arg0));

	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getAttributePrefix(int)
	 */
	public String getAttributePrefix(int arg0) {
		return stringCache.intern(super.getAttributePrefix(arg0));
	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getAttributeValue(int)
	 */
	public String getAttributeValue(int arg0) {
		return stringCache.intern(super.getAttributeValue(arg0));

	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getNamespace(java.lang.String)
	 */
	public String getNamespace(String arg0) {
		return stringCache.intern(super.getNamespace(arg0));

	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getNamespaceUri(int)
	 */
	public String getNamespaceUri(int arg0) {
		return stringCache.intern(super.getNamespaceUri(arg0));
	}
	
	/* (non-Javadoc)
	 * @see org.kxml2.io.KXmlParser#getText()
	 */
	public String getText() {
		return stringCache.intern(super.getText());

	}
	
	public String getName() {
		return stringCache.intern(super.getName());
	}
}
