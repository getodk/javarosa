package org.javarosa.services.transport.impl;

/**
 * TransportExceptions are used to wrap other exceptions to provide
 * applications which make use of the TransportService a clear indication
 * of the provenance of the exceptions
 *
 */
public class TransportException extends Exception {
	
	private Exception underlyingException;

	public TransportException(Exception underlyingException) {
		super();
		this.underlyingException = underlyingException;
	}
	
	public String getMessage(){
		return "TransportLayer exception ("+this.underlyingException.getClass()+") : "+this.underlyingException.getMessage();
	}

}
