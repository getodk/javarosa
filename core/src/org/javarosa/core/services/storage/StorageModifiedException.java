package org.javarosa.core.services.storage;

public class StorageModifiedException extends RuntimeException {
	public StorageModifiedException() {
		super();
	}

	public StorageModifiedException(String message) {
		super(message);
	}
}
