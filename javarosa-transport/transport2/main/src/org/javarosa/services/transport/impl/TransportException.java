package org.javarosa.services.transport.impl;

/**
 * TransportExceptions are used to wrap other exceptions to provide
 * applications which make use of the TransportService a clear indication
 * of the provenance of the exceptions
 *
 */
public class TransportException extends Exception {
	
	/**
	 * @param underlyingException
	 */
	public TransportException(Exception underlyingException) {
		super("TransportLayer exception ("+underlyingException.getClass()+") : "+underlyingException.getMessage());
	}
	
	public TransportException(String msg) {
		super(msg);
	}

}
