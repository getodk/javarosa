package org.javarosa.core.model.utils;

import org.javarosa.core.model.data.IAnswerData;

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
	 * Takes in a set of preload parameters, and determines the 
	 * proper IAnswerData to be preloaded for a question.
	 * 
	 * @param preloadParams the parameters determining the preload value
	 * @return An IAnswerData to be used as the default, preloaded value
	 * for a Question.
	 */
	IAnswerData handle(String preloadParams);
}
