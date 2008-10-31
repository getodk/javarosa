package org.javarosa.core.util.externalizable;

/**
 * Thrown when trying to create an object during serialization, but object cannot be created.
 */
public class CannotCreateObjectException extends RuntimeException {
	public CannotCreateObjectException(String message) {
		super(message);
	}
}
