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

package org.javarosa.core.model.utils;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeElement;

/**
 * An IPreloadHandler is capable of taking in a set of parameters
 * for a question's preloaded value, and returning an IAnswerData
 * object that should be preloaded for a question. 
 * 
 * @author Clayton Sims
 *
 */
public interface IPreloadHandler {
	
	/**
	 * @return A String representing the preload handled by this handler
	 */
	String preloadHandled(); 
	
	/**
	 * Takes in a set of preload parameters, and determines the 
	 * proper IAnswerData to be preloaded for a question.
	 * 
	 * @param preloadParams the parameters determining the preload value
	 * @return An IAnswerData to be used as the default, preloaded value
	 * for a Question.
	 */
	IAnswerData handlePreload(String preloadParams);
	
	/**
	 * Handles any post processing tasks that should be completed after the form entry
	 * interaction is completed.
	 * 
	 * @param model The completed data model.
	 * @param ref The reference to be processed
	 * @param params Processing paramaters.
	 * @return true if any post-processing occurs, false otherwise.
	 */
	boolean handlePostProcess(TreeElement node, String params);
	
}
