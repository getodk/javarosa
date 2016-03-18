package org.javarosa.core.log;

public class FatalException extends WrappedException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 72929242925662685L;

	public FatalException () {
		this("");
	}
	
	public FatalException (String message) {
		super(message);
	}
	
	public FatalException (Exception child) {
		super(child);
	}
	
	public FatalException (String message, Exception child) {
		super(message, child);
	}
	
}
