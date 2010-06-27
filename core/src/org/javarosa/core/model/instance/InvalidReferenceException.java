/**
 * 
 */
package org.javarosa.core.model.instance;

/**
 * An Invalid Reference exception is thrown whenever
 * a valid TreeReference is expected by an operation.
 * 
 * @author ctsims
 *
 */
public class InvalidReferenceException extends Exception {
	TreeReference invalid;
	
	public InvalidReferenceException(String message, TreeReference reference) {
		super(message);
		this.invalid = reference;
	}
	
	public TreeReference getInvalidReference() {
		return invalid;
	}
}
