/**
 * 
 */
package org.javarosa.core.util;

/**
 * Static utility functions for mathematical operations
 * 
 * @author ctsims
 *
 */
public class MathUtils {
	
	//a - b * floor(a / b)
	public static long modLongNotSuck (long a, long b) {
		return ((a % b) + b) % b;
	}

	public static long divLongNotSuck (long a, long b) {
		return (a - modLongNotSuck(a, b)) / b;
	}
}
