package org.javarosa.entities;

import org.javarosa.entities.internal.EntityFormParseProcessor;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;

import java.io.Reader;

public class EntityXFormParserFactory implements IXFormParserFactory {

    private final IXFormParserFactory wrapped;

    public EntityXFormParserFactory(IXFormParserFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public XFormParser getXFormParser(Reader reader) {
        return configureEntityParsing(wrapped.getXFormParser(reader));
    }

    @Override
    public XFormParser getXFormParser(Document doc) {
        return configureEntityParsing(wrapped.getXFormParser(doc));
    }

    @Override
    public XFormParser getXFormParser(Reader form, Reader instance) {
        return configureEntityParsing(wrapped.getXFormParser(form, instance));
    }

    @Override
    public XFormParser getXFormParser(Document form, Document instance) {
        return configureEntityParsing(wrapped.getXFormParser(form, instance));
    }

    private XFormParser configureEntityParsing(XFormParser xFormParser) {
        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        xFormParser.addBindAttributeProcessor(processor);
        xFormParser.addFormDefProcessor(processor);

        return xFormParser;
    }

}
