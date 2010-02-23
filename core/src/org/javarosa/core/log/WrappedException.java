package org.javarosa.core.log;

public class WrappedException extends RuntimeException {

	String message;
	Exception child;
	
	public WrappedException () {

	}
	
	public WrappedException (String message) {
		this(message, null);
	}
	
	public WrappedException (Exception child) {
		this(null, child);
	}
	
	public WrappedException (String message, Exception child) {
		this.message = message;
		this.child = child;
	}

	public String getMessage () {
		String str = "";
		if (message != null) {
			str += message;
		}
		if (child != null) {
			str += (message != null ? " => " : "") + printException(child);
		}
		return (str.equals("") ? "[exception]" : str);
	}
	
	public static String printException (Exception e) {
		if (e instanceof WrappedException) {
			return (e instanceof FatalException ? "FATAL: " : "") + e.getMessage();
		} else {
			return e.getClass().getName() + "[" + e.getMessage() + "]";
		}
	}
	
}
