package org.javarosa.xpath;

public class XPathTypeMismatchException extends XPathException {
	public XPathTypeMismatchException () {
		
	}
	
	public XPathTypeMismatchException (String s) {
		super("type mismatch " + s);
	}
}
