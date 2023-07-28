package org.javarosa.entities;

import org.javarosa.entities.internal.EntityFormParseProcessor;
import org.javarosa.xform.parse.IXFormParserFactory;
import org.javarosa.xform.parse.XFormParser;
import org.jetbrains.annotations.NotNull;

public class EntityXFormParserFactory extends IXFormParserFactory.Wrapper {

    public EntityXFormParserFactory(IXFormParserFactory base) {
        super(base);
    }

    @Override
    public XFormParser apply(@NotNull XFormParser xFormParser) {
        EntityFormParseProcessor processor = new EntityFormParseProcessor();
        xFormParser.addProcessor(processor);

        return xFormParser;
    }
}
