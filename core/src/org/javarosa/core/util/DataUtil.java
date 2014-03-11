/**
 * 
 */
package org.javarosa.core.util;

import java.util.Vector;

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
				iarray[i] = new Integer(i + low);
			}
		}
		return ivalue < high && ivalue >= low ? iarray[ivalue + offset] : new Integer(ivalue);
	}


	public static Vector<Integer> union(Vector<Integer> a, Vector<Integer> b) {
		Vector<Integer> u = new Vector<Integer>();
		//Efficiency?
		for(Integer i : a) {
			if(b.contains(i)) {
				u.addElement(i);
			}
		}
		return u;
	}
}
