package org.javarosa.xform.parse;

import java.io.Reader;

import org.kxml2.kdom.Document;

/**
 * Interface for class factory for creating an XFormParser.
 * Supports experimental extensions of XFormParser.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public interface IXFormParserFactory {
	public XFormParser getXFormParser(Reader reader);
	
	public XFormParser getXFormParser(Document doc);
	
	public XFormParser getXFormParser(Reader form, Reader instance);
	
	public XFormParser getXFormParser(Document form, Document instance);

}
