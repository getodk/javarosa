/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.model.utils;

import org.javarosa.core.Context;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.TreeElement;

/**
 * The Context Preload Handler retrieves values from a context
 * object for preloading questions.
 *  
 * @author Alfred Mukudu
 *
 */
public class ContextPreloadHandler implements IPreloadHandler
{
	public Context context;

	public ContextPreloadHandler()
	{

	}
	public ContextPreloadHandler(Context context)
	{
		this.context = context;
		//initHandler();
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePostProcess(org.javarosa.core.model.IFormDataModel, org.javarosa.core.model.IDataReference, java.lang.String)
	 */
	public boolean handlePostProcess(TreeElement node, String params) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#handlePreload(java.lang.String)
	 */
	public IAnswerData handlePreload(String preloadParams) {
		return preloadContext(preloadParams);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.IPreloadHandler#preloadHandled()
	 */
	public String preloadHandled() {
		return "context";
	}
	//The context preload parameter
	private IAnswerData preloadContext(String preloadParams) {
		String value = "";
		
		//#if javarosa.adduser.extended
		if ("UserName".equalsIgnoreCase(preloadParams)) {
			String userVal = this.context.getCurrentUser();
			System.out.println("LOGIN NAME IS "+userVal);
			
			if (userVal != null && userVal.length() > 0)
				value = userVal;
		} else if( "UserID".equalsIgnoreCase(preloadParams)) {
			Integer userID = this.context.getCurrentUserID();
			System.out.println("USER ID is "+userID);
			
			if (userID != null )
				value = userID.toString();
		}
		//#else
		if ("UserID".equalsIgnoreCase(preloadParams)) {
			String userVal = this.context.getCurrentUser();
			System.out.println("LOGIN NAME IS "+userVal);
			
			if (userVal != null && userVal.length() > 0)
				value = userVal;
		}
		//#endif
	
		System.out.println(value);
		return new StringData(value);
	}

}