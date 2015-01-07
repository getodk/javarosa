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

import java.util.ArrayList;
import java.util.List;

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

   public static <E> List<E> listCopy(List<E> a) {
      if(a == null ) { return null; }
      List<E> b = new ArrayList<E>(a.size());
      for(E e : a) {
         b.add(e);
      }
      return b;
   }

}
