package org.javarosa.core.util.externalizable;

/**
 * Thrown when trying to create an object during serialization, but object cannot be created because:
 * 
 * 1) We don't know what object to create
 *  
 * @author Clayton Sims
 *
 */
public class DeserializationException extends Exception {
	public DeserializationException(String message) {
		super(message);
	}
}
