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
