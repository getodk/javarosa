package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.kxml2.kdom.Element;

public interface IElementHandler {
	/*Object*/ void handle (FormDef f, Element e, Object parent);
}
