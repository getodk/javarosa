package org.javarosa.core.util;

/**
 * Unavailable Externalizer Exceptions are thrown when an Externalizing object
 * is attempting to deserialize some data stream, and cannot proceed due to
 * a dependency on another Externalizing object that is not available.
 *  
 * @author Clayton Sims
 *
 */
public class UnavailableExternalizerException extends Exception {
	public UnavailableExternalizerException(String message) {
		super(message);
	}
}
