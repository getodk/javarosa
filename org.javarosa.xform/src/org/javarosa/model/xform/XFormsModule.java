package org.javarosa.model.xform;

import org.javarosa.core.Context;
import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.api.IModule;
import org.javarosa.xpath.XPathParseTool;

public class XFormsModule implements IModule {

	public void registerModule(Context context) {
		String[] classes = {
				"org.javarosa.model.xform.XPathReference",
				"org.javarosa.xpath.XPathConditional"
		};
		
		JavaRosaServiceProvider.instance().registerPrototypes(classes);
		JavaRosaServiceProvider.instance().registerPrototypes(XPathParseTool.xpathClasses);

	}

}
