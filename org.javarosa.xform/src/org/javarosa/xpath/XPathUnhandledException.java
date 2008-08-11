package org.javarosa.xpath;

public class XPathUnhandledException extends XPathException {
	public XPathUnhandledException () {
		
	}
	
	public XPathUnhandledException (String s) {
		super("cannot handle " + s);
	}
}
