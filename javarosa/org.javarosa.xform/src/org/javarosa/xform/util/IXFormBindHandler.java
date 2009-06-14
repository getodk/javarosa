package org.javarosa.xform.util;

import org.javarosa.core.model.DataBinding;
import org.javarosa.core.model.FormDef;
import org.kxml2.kdom.Element;

/**
 * An IXFormBindHandler receives the xml bind node, along
 * with its default parsing, and performs some custom action.
 * 
 * @author Clayton Sims
 *
 */
public interface IXFormBindHandler {
	void handle(Element bindElement, DataBinding bind);
	void init();
	void postProcess(FormDef formDef);
}
