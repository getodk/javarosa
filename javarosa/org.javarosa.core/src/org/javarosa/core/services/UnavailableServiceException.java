package org.javarosa.core.services;

public class UnavailableServiceException extends Exception {
	public UnavailableServiceException() {
		
	}
	
	public UnavailableServiceException(String message) {
		super(message);
	}
}
