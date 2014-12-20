package org.javarosa.xform.parse;

import java.io.Reader;

import org.javarosa.core.util.CacheTable;
import org.kxml2.kdom.Document;

/**
 * Class factory for creating an XFormParser.
 *
 * This factory allows you to provide a custom string cache
 * to be used during parsing, which should be helpful
 * in conserving memories in environments where there might be
 * multiple parsed forms in memory at the same time.
 *
 * @author mitchellsundt@gmail.com / csims@dimagi.com
 *
 */
public class XFormParserFactory implements IXFormParserFactory {
	CacheTable<String> stringCache;

	public XFormParserFactory() {
	}

	public XFormParserFactory(CacheTable<String> stringCache) {
		this.stringCache = stringCache;
	}

	public XFormParser getXFormParser(Reader reader) {
		XFormParser parser = new XFormParser(reader);
		init(parser);
		return parser;
	}

	private void init(XFormParser parser) {
		if(stringCache != null) {
			parser.setStringCache(stringCache);
		}
	}

	public XFormParser getXFormParser(Document doc) {
		XFormParser parser = new XFormParser(doc);
		init(parser);
		return parser;
	}

	public XFormParser getXFormParser(Reader form, Reader instance) {
		XFormParser parser = new XFormParser(form, instance);
		init(parser);
		return parser;
	}

	public XFormParser getXFormParser(Document form, Document instance) {
		XFormParser parser = new XFormParser(form, instance);
		init(parser);
		return parser;
	}

}