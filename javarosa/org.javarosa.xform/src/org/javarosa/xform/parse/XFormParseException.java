package org.javarosa.xform.parse;

/**
 * Exception thrown when an XForms Parsing error occurs.
 * 
 * @author Drew Roos
 *
 */
// Clayton Sims - Aug 18, 2008 : This doesn't actually seem
// to be a RuntimeException to me. Is there justification
// as to why it is?
public class XFormParseException extends RuntimeException {
	public XFormParseException () { }
	
	public XFormParseException (String msg) {
		super(msg);
	}
}
