/**
 *
 */
package org.javarosa.core.util;

/**
 * @author ctsims
 *
 */
public class DataUtil {
	static final int offset = 10;
	static final  int low = -10;
	static final  int high = 400;
	static Integer[] iarray;


	public static Integer integer(int ivalue) {
		if(iarray == null) {
			iarray = new Integer[high - low];
			for(int i = 0; i < iarray.length; ++i) {
				iarray[i] = Integer.valueOf(i + low);
			}
		}
		return ivalue < high && ivalue >= low ? iarray[ivalue + offset] : Integer.valueOf(ivalue);
	}

}
