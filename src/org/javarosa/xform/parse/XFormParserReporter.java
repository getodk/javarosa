/**
 * 
 */
package org.javarosa.xform.parse;

import java.io.PrintStream;

/**
 * A Parser Reporter is provided to the XFormParser to receive
 * warnings and errors from the parser. 
 * 
 * @author ctsims
 */
public class XFormParserReporter {
	public static final String TYPE_UNKNOWN_MARKUP = "markup";
	public static final String TYPE_INVALID_STRUCTURE ="invalid-structure";
	public static final String TYPE_ERROR_PRONE ="dangerous";
	public static final String TYPE_TECHNICAL ="technical";
	protected static final String TYPE_ERROR = "error";
	
	PrintStream errorStream;
	
	public XFormParserReporter() {
		this(System.err);
	}
	
	public XFormParserReporter(PrintStream errorStream) {
		this.errorStream = errorStream;
	}
	
	public void warning(String type, String message, String xmlLocation) {
		errorStream.println("XForm Parse Warning: " + message + (xmlLocation == null ? "" : xmlLocation));
	}
	
	public void error(String message) {
		errorStream.println("XForm Parse Error: " + message);
	}
}
