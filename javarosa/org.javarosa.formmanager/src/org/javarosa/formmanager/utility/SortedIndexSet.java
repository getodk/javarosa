package org.javarosa.formmanager.utility;

import java.util.Vector;

import org.javarosa.core.model.FormIndex;

/**
 * Copied from Drew's SortedIntSet class.
 * 
 * Maintains an ordered set of Form Indices without duplicates
 * 
 * @author Clayton Sims (kind of)
 *
 */
public class SortedIndexSet {
	Vector v;
	
	public SortedIndexSet () {
		v = new Vector();
	}
		
	//add new value; return index inserted at if value was not already present, -1 if it was
	public int add (FormIndex index) {
		int i = indexOf(index, false);
		if (i != -1 && get(i) == index) {
			return -1;
		} else {
			v.insertElementAt(index, i + 1);
			return i + 1;
		}
	}
	
	//remove a value; return index of item just removed if it was present, -1 if it was not
	public int remove (FormIndex n) {
		int i = indexOf(n, true);
		if (i != -1)
			v.removeElementAt(i);
		return i;
	}
	
	//return value at index
	public FormIndex get (int i) {
		return (FormIndex)v.elementAt(i);
	}
	
	//return whether value is present
	public boolean contains (FormIndex n) {
		return (indexOf(n, true) != -1);
	}
	
	//if exact = true: return the index of a value, -1 if not present
	//if exact = false: return the index of the highest value <= the target value, -1 if all values are greater than the target value
	public int indexOf (FormIndex index, boolean exact) {
		int lo = 0;
		int hi = v.size() - 1;

		while (lo <= hi) {
			int mid = (lo + hi) / 2;
			FormIndex val = get(mid);

			if (val.compareTo(index) < 0) {
				lo = mid + 1;
			} else if (val.compareTo(index) > 0) {
				hi = mid - 1;
			} else {
				return mid;
			}
		}

		return exact ? -1 : lo - 1;
	}
	
	//return number of values
	public int size () {
		return v.size();
	}
	
	//return underlying vector (outside modification may corrupt the datastructure)
	public Vector getVector () {
		return v;
	}
}