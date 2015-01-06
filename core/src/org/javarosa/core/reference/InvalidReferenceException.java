/**
 * 
 */
package org.javarosa.core.reference;

/**
 * An invalid reference exception is thrown whenever
 * a URI string cannot be resolved to a reference in
 * the current environment. Just because an invalid 
 * reference exception is not thrown does not mean
 * that there is a binary data blob at the created
 * reference, only that it has meaning and could refer
 * to something in the current environment.
 * 
 * @author ctsims
 *
 */
public class InvalidReferenceException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2062070187063973388L;
	
	private String reference;
	
	/**
	 * A new exception implying that a URI could not be resolved to
	 * a reference.
	 * @param message The failure message for why the URI could not be
	 * resolved.
	 * @param reference The URI which was unable to be resolved.
	 */
	public InvalidReferenceException(String message, String reference) {
		super(message);
		this.reference = reference;
	}
	public String getReferenceString() {
		return reference;
	}
}
