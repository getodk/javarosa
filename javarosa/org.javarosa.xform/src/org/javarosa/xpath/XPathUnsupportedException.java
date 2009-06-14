package org.javarosa.xpath;

public class XPathUnsupportedException extends XPathException {
	public XPathUnsupportedException () {
		
	}
	
	public XPathUnsupportedException (String s) {
		super("unsupported construct [" + s + "]");
	}
}
