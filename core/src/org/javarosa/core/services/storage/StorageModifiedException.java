package org.javarosa.core.services.storage;

public class StorageModifiedException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8547516967488722190L;

	public StorageModifiedException() {
		super();
	}

	public StorageModifiedException(String message) {
		super(message);
	}
}
