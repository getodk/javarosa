package org.javarosa.core.util;

/**
 * IncompatibleViewException is thrown whenever a View is attempted
 * to be set on a Display that does not recognize the view.   
 * 
 * @author Clayton Sims
 *
 */
public class IncompatibleViewException extends RuntimeException {
	public IncompatibleViewException(String message) {
		super(message);
	}
}
