/**
 * 
 */
package org.javarosa.core.reference;

/**
 * @author ctsims
 *
 */
public class InvalidReferenceException extends Exception {
	private String reference;
	public InvalidReferenceException(String message, String reference) {
		super(message);
		this.reference = reference;
	}
	public String getReferenceString() {
		return reference;
	}
}
