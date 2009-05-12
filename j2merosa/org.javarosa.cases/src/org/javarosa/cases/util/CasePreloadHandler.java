/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.cases.model.Case;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.utils.IPreloadHandler;
import org.javarosa.core.model.utils.PreloadUtils;

/**
 * @author Clayton Sims
 * @date Mar 19, 2009 
 *
 */
public class CasePreloadHandler implements IPreloadHandler {
	
	Case c;
	
	public CasePreloadHandler(Case c) {
		this.c = c;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePostProcess(org.javarosa.core.model.instance.TreeElement, java.lang.String)
	 */
	public boolean handlePostProcess(TreeElement node, String params) {
		//Nothing yet!
		return false;
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePreload(java.lang.String)
	 */
	public IAnswerData handlePreload(String preloadParams) {
		Object property = c.getProperty(preloadParams);
		return PreloadUtils.wrapIndeterminedObject(property);
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "case";
	}

}
