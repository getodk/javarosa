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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

//maintain an array of integers in sorted order. no duplicates allowed.
public class SortedIntSet implements Externalizable {
	List<Integer> v;

	public SortedIntSet () {
		v = new ArrayList<Integer>(0);
	}

	//add new value; return index inserted at if value was not already present, -1 if it was
	public int add (int n) {
		int i = indexOf(n, false);
		if (i != -1 && get(i) == n) {
			return -1;
		} else {
			v.add(i+1,Integer.valueOf(n));
			return i + 1;
		}
	}

	//remove a value; return index of item just removed if it was present, -1 if it was not
	public int remove (int n) {
		int i = indexOf(n, true);
		if (i != -1)
			v.remove(i);
		return i;
	}

	//return value at index
	public int get (int i) {
		return v.get(i).intValue();
	}

	//return whether value is present
	public boolean contains (int n) {
		return (indexOf(n, true) != -1);
	}

	//if exact = true: return the index of a value, -1 if not present
	//if exact = false: return the index of the highest value <= the target value, -1 if all values are greater than the target value
	public int indexOf (int n, boolean exact) {
		int lo = 0;
		int hi = v.size() - 1;

		while (lo <= hi) {
			int mid = (lo + hi) / 2;
			int val = get(mid);

			if (val < n) {
				lo = mid + 1;
			} else if (val > n) {
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
	public List<Integer> getList () {
		return v;
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		v = (List<Integer>)ExtUtil.read(in, new ExtWrapList(Integer.class));
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.write(out, new ExtWrapList(v));
	}
}