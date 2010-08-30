/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

/**
 * 
 */
package org.javarosa.cases.util;

import org.javarosa.cases.model.Case;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.UncastData;
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
		 if("name".equals(preloadParams)) {
			return new StringData(c.getName());
		 } else if("external-id".equals(preloadParams)) {
			return new StringData(c.getExternalId());
	 	 } else if("status".equals(preloadParams)) {
		 	if(c.isClosed()) {
				return new StringData("closed");
			} else {
				return new StringData("open");
			}
		} else if("date-opened".equals(preloadParams)) {
			return new DateData(c.getDateOpened());
		} else {
			Object retVal = c.getProperty(preloadParams);
			if(retVal instanceof String) {
				return new UncastData((String)retVal);
			}
			return PreloadUtils.wrapIndeterminedObject(retVal);
		}
	}

	/* (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "case";
	}

}
