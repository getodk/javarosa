package org.javarosa.xform.parse;

import org.javarosa.core.model.FormDef;
import org.kxml2.kdom.Element;

/**
 * An IElementHandler is responsible for handling the parsing of a particular
 * XForms node.
 *  
 * @author Drew Roos
 *
 */
public interface IElementHandler {
	/*Object*/ void handle (FormDef f, Element e, Object parent);
}
