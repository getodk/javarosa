package org.javarosa.xform.parse;

import org.jetbrains.annotations.NotNull;
import org.kxml2.kdom.Document;

import java.io.Reader;

/**
 * Interface for class factory for creating an XFormParser.
 * Supports experimental extensions of XFormParser.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public interface IXFormParserFactory {
    XFormParser getXFormParser(Reader reader);

    XFormParser getXFormParser(Document doc);

    XFormParser getXFormParser(Reader form, Reader instance);

    XFormParser getXFormParser(Document form, Document instance);

    abstract class Wrapper implements IXFormParserFactory {

        private final IXFormParserFactory base;

        public Wrapper(@NotNull IXFormParserFactory base) {
            this.base = base;
        }

        public abstract XFormParser apply(@NotNull XFormParser xFormParser);

        @Override
        public XFormParser getXFormParser(Reader reader) {
            return apply(base.getXFormParser(reader));
        }

        @Override
        public XFormParser getXFormParser(Document doc) {
            return apply(base.getXFormParser(doc));
        }

        @Override
        public XFormParser getXFormParser(Reader form, Reader instance) {
            return apply(base.getXFormParser(form, instance));
        }

        @Override
        public XFormParser getXFormParser(Document form, Document instance) {
            return apply(base.getXFormParser(form, instance));
        }
    }
}
