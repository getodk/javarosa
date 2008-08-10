package org.javarosa.xpath;

import java.util.Vector;

public interface IFunctionHandler {
	/* Name of function being handled */
	String getName ();
	
	/* Vector of allowed prototypes for this function. Each prototype is an array
	 * of Class, corresponding to the types of the expected arguments
	 * The first matching prototype is used.
	 */
	Vector getPrototypes ();
	
	/* Return true if this handler should be fed the raw argument list if no prototype matches it */
	boolean rawArgs ();
	
	/* Evaluate the function */
	Object eval (Object[] args);
}
