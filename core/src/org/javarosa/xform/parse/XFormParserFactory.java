package org.javarosa.xform.parse;

import java.io.Reader;

import org.kxml2.kdom.Document;

/**
 * Class factory for creating an XFormParser.
 * Supports experimental extensions of XFormParser.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class XFormParserFactory implements IXFormParserFactory {

	public XFormParserFactory() {
	}
	
	public XFormParser getXFormParser(Reader reader) {
		return new XFormParser(reader);
	}
	
	public XFormParser getXFormParser(Document doc) {
		return new XFormParser(doc);
	}
	
	public XFormParser getXFormParser(Reader form, Reader instance) {
		return new XFormParser(form, instance);
	}
	
	public XFormParser getXFormParser(Document form, Document instance) {
		return new XFormParser(form, instance);
	}

}
