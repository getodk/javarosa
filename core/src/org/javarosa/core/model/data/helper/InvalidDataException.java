/**
 * 
 */
package org.javarosa.core.model.data.helper;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.UncastData;

/**
 * @author ctsims
 *
 */
public class InvalidDataException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3780913062960321186L;
	
	UncastData standin;

	public InvalidDataException(String message, UncastData standin) {
		super(message);
		this.standin = standin;
	}

	public IAnswerData getUncastStandin() {
		return standin;
	}

}
