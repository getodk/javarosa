package org.javarosa.xpath;

public class XPathException extends RuntimeException {
	public XPathException () {
		
	}
	
	public XPathException (String s) {
		super("XPath evaluation: " + s);
	}
}
