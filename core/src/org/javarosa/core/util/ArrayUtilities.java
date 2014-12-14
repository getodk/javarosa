/*
 * Copyright (C) 2009 JavaRosa
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.javarosa.core.util;

import java.util.Vector;

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

		for(int i = 0 ; i < array1.length ; ++i ) {
			if(!array1[i].equals(array2[i])) {
				return false;
			}
		}
		return true;
	}

	public static boolean arraysEqual(byte[] array1, byte[] array2) {
		if(array1.length != array2.length) {
			return false;
		}

		for(int i = 0 ; i < array1.length ; ++i ) {
			if(array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}


	public static boolean arraysEqual(char[] array1, int a1start, char[] array2, int a2start) {
		if(array1.length - a1start != array2.length - a2start) {
			return false;
		}

		for(int i = 0 ; i < array1.length - a1start ; ++i ) {
			if(array1[i + a1start] != array2[i + a2start]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Find a single intersecting element common to two lists, or null if none
	 * exists. Note that no unique condition will be reported if there are multiple
	 * elements which intersect, so this should likely only be used if the possible
	 * size of intersection is 0 or 1
	 *
	 * @param a
	 * @param b
	 * @return
	 */
	public static <E> E intersectSingle(Vector<E> a, Vector<E> b) {
		for(E e : a) {
			if(b.indexOf(e) != -1) {
				return e;
			}
		}
		return null;
	}

	public static <E> Vector<E> vectorCopy(Vector<E> a) {
		if(a == null ) { return null; }
		Vector<E> b = new Vector<E>(a.size());
		for(E e : a) {
			b.addElement(e);
		}
		return b;
	}

	public static <E> E[] copyIntoArray(Vector<E> v, E[] a) {
		int i = 0;
		for(E e : v) {
			a[i++] = e;
		}
		return a;
	}

	public static <E> Vector<E> toVector(E[] a) {
		Vector<E> v = new Vector<E>(a.length);
		for(E e : a) {
			v.addElement(e);
		}
		return v;
	}
}
