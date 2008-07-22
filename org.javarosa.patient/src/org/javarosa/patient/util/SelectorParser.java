package org.javarosa.patient.util;

import java.util.Vector;

/**
 * The Selector Parser takes in a vector of elements, and returns a subset of
 * them based on a selector string of the proper format.
 * 
 * Valid formats for selector strings include 
 * [a] : Select element a
 * [a:c] : Select elements between a and c
 * [a:b:] : Select b elements starting at index a
 * [:b:c] : Select b elements starting at index b-c
 * 
 * Where a and c are concrete numerical references, ('N' as a value is reserved for the
 * last index of a vector), and b is an offset.
 * 
 * The SelectorParser makes a best-faith effort to deliver data, even when bounds are
 * crossed with concrete references or offsets. IE: for a vector with only 6 elements,
 * the Selector '[:10:N]' will return all 6 elements, even though the initial bound is
 * technically -4. This allows for fairly optimistic referencing, even when conditions
 * cannot be ensured.
 * 
 * @author Clayton Sims
 *
 */
public class SelectorParser {
	//TODO: This class begs for a unit test

	/**
	 * Selects a subset of the given vector based on the selector string
	 * passed in.
	 *  
	 * @param selector The selector string to be used to filter the elements
	 * @param elements The list of elements to be filtered
	 * @return A list of elements which is a subset of the given vector,
	 * based on the breakdown for a properly formatted selector screen
	 * described in this class's overview.
	 */
	public static Vector selectValues(String selector, Vector elements) {
		int firstIndex = 0;
		int lastIndex = elements.size()-1;
		
		selector = selector.substring(1, selector.length()-1);
		int firstColon = selector.indexOf(':');
		int secondColon = selector.indexOf(':', firstColon + 1);
		if(secondColon == -1) {
			secondColon = firstColon;
		}
		//Single selection, [n] 
		if(firstColon == -1) {
			//[N]
			if(selector == "N") {
				firstIndex = lastIndex;
			}
			//[n]
			else {
				int index = Integer.parseInt(selector);
				firstIndex = boundIndex(index, lastIndex);
				lastIndex = firstIndex;
			}
		}
		//[a:b:c]
		else {
			String a = selector.substring(0, firstColon);
			String c = selector.substring(secondColon, selector.length()-1);
			//[a:c]
			if(secondColon == firstColon) {
				firstIndex = boundIndex(Integer.parseInt(a), lastIndex);
				lastIndex = boundIndex(Integer.parseInt(c), lastIndex);
			}
			//[a:b:] or [:b:c]
			else {
				String b = selector.substring(firstColon, secondColon+1);
				//[:b:c]
				if(a == "") {
					int cIndex;
					if(c == "N") {
						cIndex = lastIndex;
					}
					else {
						cIndex = Integer.parseInt(c);
					}
					lastIndex = boundIndex(cIndex, lastIndex);
					firstIndex = boundIndex(lastIndex - Integer.parseInt(b), lastIndex); 
				}
				//[a:b]
				else if(c == "") {
					firstIndex = boundIndex(Integer.parseInt(a), lastIndex);
					lastIndex = boundIndex(firstIndex + Integer.parseInt(b), lastIndex);
				}
			}
		}
		return subVector(elements, firstIndex, lastIndex);
	}
	
	/** 
	 * @param v The vector whose elements are to be returned
	 * @param a The index of the first value to be included. 
	 * requires a > 0, a < v.size()
	 * @param c The index of the last value to be included.
	 * requires c > 0, b < v.size()
	 * @return a vector containing the subset of elements v[a:c]
	 */
	private static Vector subVector(Vector v, int a, int c) {
		Vector retVector = new Vector();
		for(int i = a ; i <= c ; i++) {
			retVector.addElement(v.elementAt(i));
		}
		return retVector;
	}
	
	/**
	 * Bounds the given index 0 < index < lastIndex
	 * @param index the index to be bound
	 * @param lastIndex the upper bound 
	 * @return 0 if index < 0, lastIndex if index > lastIndex, 
	 * index otherwise.
	 */
	private static int boundIndex(int index, int lastIndex) {
		if(index < 0) {
			return 0;
		}
		if(index > lastIndex) {
			return lastIndex;
		}
		return index;
	}
	
	/**
	 * Determines whether the given selector screen is valid, and
	 * can be meaningfully parsed.
	 * 
	 * NOTE: This method is as of yet unimplemented
	 * 
	 * @param selector The selector string to test
	 * @return True if the string contains a valid selector expression,
	 * false otherwise.
	 */
	public static boolean validSelector(String selector) {
//		if(delimeters.charAt(0) != '[' || delimeters.charAt(delimeters.length() - 1) != ']') { 
//			return false;
//		}
//		delimeters = delimeters.substring(1, delimeters.length()-1);
//		int firstColon = delimeters.indexOf(':');
//		
		//TODO: Build a proper checker
		return true;
	}

}
