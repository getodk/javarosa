/**
 * 
 */
package org.javarosa.cases.util;

/**
 * @author ctsims
 *
 */
public class MalformedCaseModelException extends Exception {
	
	private String invalidElement;
	public MalformedCaseModelException(String message, String invalidElement){
		super(message);
	}
	
	public String getInvalidElement() {
		return invalidElement;
	}
}
