/**
 * 
 */
package org.javarosa.core.model.condition.pivot;

/**
 * @author ctsims
 *
 */
public class UnpivotableExpressionException extends Exception {
	
	/**
	 * Default constructor. Should be used for semanticly unpivotable
	 * expressions which are expected
	 */
	public UnpivotableExpressionException() {
		
	}

	/**
	 * Message constructor. Should be used when something unusual happens.
	 * @param message
	 */
	public UnpivotableExpressionException(String message) {
		super(message);
	}

}
