/*
 * Copyright (C) 2009 JavaRosa-Core Project
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

package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

public class TreeReference implements Externalizable {
	public static final int INDEX_UNBOUND = -1;//multiplicity
	public static final int INDEX_TEMPLATE = -2;//multiplicity
	public static final int REF_ABSOLUTE = -1;
	
	public static final String NAME_WILDCARD = "*";
	
	public int refLevel; //0 = context node, 1 = parent, 2 = grandparent ...
	public Vector names; //Vector<String>
	public Vector multiplicity; //Vector<Integer>
	
	public static TreeReference rootRef () {
		TreeReference root = new TreeReference();
		root.refLevel = REF_ABSOLUTE;
		return root;
	}
	
	public static TreeReference selfRef () {
		TreeReference self = new TreeReference();
		self.refLevel = 0;
		return self;
	}
	
	public TreeReference () {
		names = new Vector();
		multiplicity = new Vector();		
	}

	public int size () {
		return names.size();
	}
	
	public void add (String name, int index) {
		names.addElement(name);
		multiplicity.addElement(new Integer(index));
	}
	
	public boolean isAbsolute () {
		return refLevel == REF_ABSOLUTE;
	}
	
	//return a copy of the ref
	public TreeReference clone () {
		TreeReference newRef = new TreeReference();
		newRef.refLevel = refLevel;
		for (int i = 0; i < size(); i++) {
			newRef.names.addElement(names.elementAt(i));
			newRef.multiplicity.addElement(multiplicity.elementAt(i));
		}
		return newRef;
	}
	
	/*
	 * chop the lowest level off the ref so that the ref now represents the parent of the original ref
	 * return true if we successfully got the parent, false if there were no higher levels
	 */
	public boolean removeLastLevel () {
		int size = size();
		if (size == 0) {
			if (refLevel == REF_ABSOLUTE) {
				return false;
			} else {
				refLevel++;
				return true;
			}
		} else {
			names.removeElementAt(size - 1);
			multiplicity.removeElementAt(size - 1);
			return true;
		}
	}
	
	public TreeReference getParentRef () {
		TreeReference ref = this.clone();
		if (ref.removeLastLevel()) {
			return ref;
		} else {
			return null;
		}
	}
	
	//return a new reference that is this reference anchored to a passed-in parent reference
	//if this reference is absolute, return self
	//if this ref has 'parent' steps (..), it can only be anchored if the parent ref is a relative ref consisting only of other 'parent' steps
	//return null in these invalid situations
	public TreeReference parent (TreeReference parentRef) {
		if (refLevel == REF_ABSOLUTE) {
			return this;
		} else {
			TreeReference newRef = parentRef.clone();

			if (refLevel > 0) {
				if (parentRef.refLevel != REF_ABSOLUTE && parentRef.size() == 0) {
					parentRef.refLevel += refLevel;
				} else {
					return null;
				}
			}
			
			for (int i = 0; i < names.size(); i++) {
				newRef.add((String)names.elementAt(i), ((Integer)multiplicity.elementAt(i)).intValue());
			}

			return newRef;			
		}
	}
	
	
	//very similar to parent(), but assumes contextRef refers to a singular, existing node in the model
	//this means we can do '/a/b/c + ../../d/e/f = /a/d/e/f', which we couldn't do in parent()
	//return null if context ref is not absolute, or we parent up past the root node
	//NOTE: this function still works even when contextRef contains INDEX_UNBOUND multiplicites... conditions depend on this behavior,
	//  even though it's slightly icky
	public TreeReference anchor (TreeReference contextRef) {
		if (refLevel == REF_ABSOLUTE) {
			return this.clone();
		} else if (contextRef.refLevel != REF_ABSOLUTE) {
			return null;
		} else {
			TreeReference newRef = contextRef.clone();
			int contextSize = contextRef.size();
			if (refLevel > contextSize) {
				return null; //tried to do '/..'
			} else {			
				for (int i = 0; i < refLevel; i++) {
					newRef.removeLastLevel();
				}
				for (int i = 0; i < size(); i++) {
					newRef.add((String)names.elementAt(i), ((Integer)multiplicity.elementAt(i)).intValue());
				}		
				return newRef;
			}
		}
	}
	
	//TODO: merge anchor() and parent()
		
	public TreeReference contextualize (TreeReference contextRef) {
		if (contextRef.refLevel != REF_ABSOLUTE)
			return null;
		
		TreeReference newRef = anchor(contextRef);
		
		for (int i = 0; i < contextRef.size() && i < newRef.size(); i++) {
			if (((String)contextRef.names.elementAt(i)).equals(newRef.names.elementAt(i))) {
				newRef.multiplicity.setElementAt(contextRef.multiplicity.elementAt(i), i);
			} else {
				break;
			}
		}

		return newRef;
	}
	
	//turn unambiguous ref into a generic ref
	public TreeReference genericize () {	
		TreeReference genericRef = clone();
		for (int i = 0; i < genericRef.size(); i++) {
			genericRef.multiplicity.setElementAt(new Integer(TreeReference.INDEX_UNBOUND), i);
		}
		return genericRef;
	}
	
	//returns true if 'this' is parent of 'child'
	//return true if 'this' equals 'child' only if properParent is false
	public boolean isParentOf (TreeReference child, boolean properParent) {
		if (refLevel != child.refLevel)
			return false;
		if (child.size() < size() + (properParent ? 1 : 0))
			return false;
		
		for (int i = 0; i < size(); i++) {
			if (!((String)names.elementAt(i)).equals((String)child.names.elementAt(i))) {
				return false;
			}
			
			int parMult = ((Integer)multiplicity.elementAt(i)).intValue();
			int childMult = ((Integer)child.multiplicity.elementAt(i)).intValue();
			if (parMult != INDEX_UNBOUND && parMult != childMult && !(i == 0 && parMult == 0 && childMult == INDEX_UNBOUND)) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean equals (Object o) {
		if (this == o) {
			return true;
		} else if (o instanceof TreeReference) {
			TreeReference ref = (TreeReference)o;
			
			if (this.refLevel == ref.refLevel && this.size() == ref.size()) {
				for (int i = 0; i < this.size(); i++) {
					String nameA = (String)this.names.elementAt(i);
					String nameB = (String)ref.names.elementAt(i);
					int multA = ((Integer)this.multiplicity.elementAt(i)).intValue();
					int multB = ((Integer)ref.multiplicity.elementAt(i)).intValue();
					
					if (!nameA.equals(nameB)) {
						return false;
					} else if (multA != multB) {
						if (i == 0 && (multA == 0 || multA == INDEX_UNBOUND) && (multB == 0 || multB == INDEX_UNBOUND)) {
							// /data and /data[0] are functionally the same
						} else {
							return false;
						}
					}
				}	
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	public int hashCode () {
		int hash = (new Integer(refLevel)).hashCode();
		for (int i = 0; i < size(); i++) {
			Integer mult = (Integer)multiplicity.elementAt(i);
			if (i == 0 && mult.intValue() == INDEX_UNBOUND)
				mult = new Integer(0);
			
			hash ^= ((String)names.elementAt(i)).hashCode();
			hash ^= mult.hashCode();
		}
		return hash;
	}
	
	public String toString () {
		return toString(true);
	}
	
	public String toString (boolean includePredicates) {
		StringBuffer sb = new StringBuffer();
		if (refLevel == REF_ABSOLUTE) {
			sb.append("/");
		} else {
			for (int i = 0; i < refLevel; i++)
				sb.append("../");
		}
		for (int i = 0; i < size(); i++) {
			String name = (String)names.elementAt(i);
			int mult = ((Integer)multiplicity.elementAt(i)).intValue();
			
			sb.append(name);
			
			if (includePredicates) {
				switch (mult) {
				case INDEX_UNBOUND: break;
				case INDEX_TEMPLATE: sb.append("[@template]"); break;
				default:
					if (i > 0 || mult != 0)
						sb.append("[" + (mult + 1) + "]");
					break;
				}
			}
			
			if (i < size() - 1)
				sb.append("/");
		}
		
		return sb.toString();
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf)
			throws IOException, DeserializationException {
		refLevel = ExtUtil.readInt(in);
		names = (Vector)ExtUtil.read(in, new ExtWrapList(String.class), pf);
		multiplicity = (Vector)ExtUtil.read(in, new ExtWrapList(Integer.class), pf);
	}

	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeNumeric(out, refLevel);
		ExtUtil.write(out, new ExtWrapList(names));
		ExtUtil.write(out, new ExtWrapList(multiplicity));
	}
}
