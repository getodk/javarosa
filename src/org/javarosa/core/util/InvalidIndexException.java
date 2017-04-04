/**
 * 
 */
package org.javarosa.core.util;

/**
 * Thrown when an index used contains an invalid value
 * 
 * @author ctsims
 *
 */
public class InvalidIndexException extends RuntimeException {
	String index;
	public InvalidIndexException(String message, String index) {
		super(message);
		this.index = index;
	}
	
	public String getIndex() {
		return index;
	}
}
