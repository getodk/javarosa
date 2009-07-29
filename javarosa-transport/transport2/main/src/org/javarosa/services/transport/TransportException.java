package org.javarosa.services.transport;

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
