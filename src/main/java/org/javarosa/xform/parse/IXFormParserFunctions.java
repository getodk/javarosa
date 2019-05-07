package org.javarosa.xform.parse;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;

/** Provides functions from XFormParser to classes split off from it, to avoid a cyclical dependency. */
public interface IXFormParserFunctions {
    IDataReference getAbsRef (IDataReference ref, IFormElement parent);
}
