package org.javarosa.core.util;

/**
 * 
 * @author Clayton Sims
 *
 */
public class ArrayUtilities {
	public static boolean arraysEqual(Object[] array1, Object[] array2) {
		if(array1.length != array2.length) {
			return false;
		}
		boolean retVal = true;
		for(int i = 0 ; i < array1.length ; ++i ) {
			if(!array1[i].equals(array2[i])) {
				retVal = false;
			}
		}
		return retVal;
	}
}
