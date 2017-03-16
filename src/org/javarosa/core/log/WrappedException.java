package org.javarosa.core.log;

public class WrappedException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6187588881484173495L;
	
	String message;
	Exception child;
	
	public WrappedException (String message) {
		this(message, null);
	}
	
	public WrappedException (Exception child) {
		this(null, child);
	}
	
	public WrappedException (String message, Exception child) {
		super(constructMessage(message, child));
		this.message = message;
		this.child = child;
	}

	public static String constructMessage (String message, Exception child) {
		String str = "";
		if (message != null) {
			str += message;
		}
		if (child != null) {
			str += (message != null ? " => " : "") + printException(child);
		}
		
		if (str.equals(""))
			str = "[exception]";		
		return str;
	}
	
	public static String printException (Exception e) {
		if (e instanceof WrappedException) {
			return (e instanceof FatalException ? "FATAL: " : "") + e.getMessage();
		} else {
			return e.getClass().getName() + "[" + e.getMessage() + "]";
		}
	}
	
}
