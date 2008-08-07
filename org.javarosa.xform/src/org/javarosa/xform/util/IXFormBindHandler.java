package org.javarosa.xform.util;

import org.javarosa.core.model.DataBinding;
import org.kxml2.kdom.Element;

public interface IXFormBindHandler {
	void handle(Element bindElement, DataBinding bind);
}
