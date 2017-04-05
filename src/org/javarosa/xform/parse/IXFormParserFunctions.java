package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.condition.Condition;
import org.javarosa.core.model.condition.Recalculate;
import org.javarosa.xpath.parser.XPathSyntaxException;

/** Provides functions from XFormParser to classes split off from it, to avoid a cyclical dependency. */
public interface IXFormParserFunctions {
    IDataReference getAbsRef (IDataReference ref, IFormElement parent);
    FormDef getFormDef();
    Condition buildCondition (String xpath, String type, IDataReference contextRef);
    Recalculate buildCalculate (String xpath, IDataReference contextRef) throws XPathSyntaxException;
    int getDataType(String type);
}
