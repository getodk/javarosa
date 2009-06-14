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

package org.javarosa.core.model;

/**
 * A Form Index is an immutable index into a specific question definition that 
 * will appear in an interaction with a user.
 * 
 * An index is represented by different levels into hierarchical groups.
 * 
 * Indices can represent both questions and groups.
 * 
 * It is absolutely essential that there be no circularity of reference in
 * FormIndex's, IE, no form index's ancestor can be itself.
 * 
 * Datatype Productions:
 * FormIndex = BOF | EOF | CompoundIndex(nextIndex:FormIndex,Location)
 * Location = Empty | Simple(localLevel:int) | WithMult(localLevel:int, multiplicity:int)
 *   
 * @author Clayton Sims
 *
 */
public class FormIndex {
	private boolean beginningOfForm = false;
	
	private boolean endOfForm = false;
	
	/** The index of the questiondef in the current context */
	private int localIndex;
	
	/** The multiplicity of the current instance of a repeated question or group */
	private int instanceIndex = -1;
	
	/** The next level of this index */
	private FormIndex nextLevel;

	public static FormIndex createBeginningOfFormIndex() {
		FormIndex begin = new FormIndex(-1);
		begin.beginningOfForm = true;
		return begin;
	}
	
	public static FormIndex createEndOfFormIndex() {
		FormIndex end = new FormIndex(-1);
		end.endOfForm = true;
		return end;
	}
	
	/**
	 * Constructs a simple form index that references a specific element in
	 * a list of elements.
	 * @param localIndex An integer index into a flat list of elements
	 */
	public FormIndex(int localIndex) {
		this.localIndex = localIndex;
		
	}
	/**
	 * Constructs a simple form index that references a specific element in
	 * a list of elements.
	 * @param localIndex An integer index into a flat list of elements
	 * @param instanceIndex An integer index expressing the multiplicity
	 * of the current level
	 * 
	 */
	public FormIndex(int localIndex, int instanceIndex) {
		this.localIndex = localIndex;
		this.instanceIndex = instanceIndex;
	}
	
	/** 
	 * Constructs an index which indexes an element, and provides an index 
	 * into that elements children 
	 * 
	 * @param nextLevel An index into the referenced element's index
	 * @param localIndex An index to an element at the current level, a child
	 * element of which will be referenced by the nextLevel index. 
	 */
	public FormIndex(FormIndex nextLevel, int localIndex) {
		this(localIndex);
		this.nextLevel = nextLevel;
	}

	/**
	 * Constructs an index which references an element past the level of 
	 * specificity of the current context, founded by the currentLevel
	 * index.
	 * (currentLevel, (nextLevel...))
	 */
	public FormIndex(FormIndex nextLevel, FormIndex currentLevel) {
		if(currentLevel == null) {
			this.nextLevel = nextLevel.nextLevel;
			this.localIndex = nextLevel.localIndex;
			this.instanceIndex = nextLevel.instanceIndex;
		} else {
			this.nextLevel = nextLevel;
			this.localIndex = currentLevel.getLocalIndex();
			this.instanceIndex = currentLevel.getInstanceIndex();
		}
	}
	
	/** 
	 * Constructs an index which indexes an element, and provides an index 
	 * into that elements children, along with the current index of a
	 * repeated instance.
	 * 
	 * @param nextLevel An index into the referenced element's index
	 * @param localIndex An index to an element at the current level, a child
	 * element of which will be referenced by the nextLevel index.
	 * @param instanceIndex How many times the element referenced has been 
	 * repeated.
	 */
	public FormIndex(FormIndex nextLevel, int localIndex, int instanceIndex) {
		this(nextLevel, localIndex);
		this.instanceIndex = instanceIndex;
	}

	public boolean isInForm () {
		return !beginningOfForm && !endOfForm;
	}
	
	/**
	 * @return The index of the element in the current context
	 */
	public int getLocalIndex() {
		return localIndex;
	}
	
	/**
	 * @return The multiplicity of the current instance of a repeated question or group
	 */
	public int getInstanceIndex() {
		return instanceIndex;
	}

	/**
	 * @return An index into the next level of specificity past the current context. An
	 * example would be an index into an element that is a child of the element referenced
	 * by the local index. 
	 */
	public FormIndex getNextLevel() {
		return nextLevel;
	}
	
	/**
	 * Identifies whether this is a terminal index, in other words whether this
	 * index references with more specificity than the current context
	 */
	public boolean isTerminal() {
		return nextLevel == null;
	}
	
	public boolean isEndOfFormIndex() {
		return endOfForm;
	}
	
	public boolean isBeginningOfFormIndex() {
		return beginningOfForm;
	}
	
	public void setNextLevel (FormIndex next) {
		this.nextLevel = next;
	}
	
	public boolean equals(Object o) {
		if(!(o instanceof FormIndex))
			return false;

		FormIndex a = this;
		FormIndex b = (FormIndex)o;
		
		return (a.compareTo(b) == 0);		
		
//		//TODO: while(true) loops freak me out, this should probably
//		//get written more safely. -ctsims
//		
//		//Iterate over each level of reference, and identify whether
//		//each object stays in sync
//		while(true) {
//			if(index.isTerminal() != local.isTerminal() ||
//					index.getLocalIndex() != local.getLocalIndex() ||
//					index.getInstanceIndex() != local.getInstanceIndex()) {
//				return false;
//			}
//			if(index.isTerminal()) {
//				return true;
//			}
//			local = local.getNextLevel();
//			index = index.getNextLevel();
//		}
//		
	}
	
	public int compareTo(Object o) {
		if(!(o instanceof FormIndex))
			throw new IllegalArgumentException("Attempt to compare Object of type " + o.getClass().getName() + " to a FormIndex");

		FormIndex a = this;
		FormIndex b = (FormIndex)o;
		
		if (a.beginningOfForm) {
			return (b.beginningOfForm ? 0 : -1);
		} else if (a.endOfForm) {
			return (b.endOfForm ? 0 : 1);
		} else {
			//a is in form
			if (b.beginningOfForm) {
				return 1;
			} else if (b.endOfForm) {
				return -1;
			}
		}

		if (a.localIndex != b.localIndex) {
			return (a.localIndex < b.localIndex ? -1 : 1);
		} else if (a.instanceIndex != b.instanceIndex) {
			return (a.instanceIndex < b.instanceIndex ? -1 : 1);
		} else if ((a.getNextLevel() == null) != (b.getNextLevel() == null)) {
			return (a.getNextLevel() == null ? -1 : 1);
		} else if (a.getNextLevel() != null) {
			return a.getNextLevel().compareTo(b.getNextLevel());
		} else {
			return 0;
		}
				
//		int comp = 0;
//		
//		//TODO: while(true) loops freak me out, this should probably
//		//get written more safely. -ctsims
//		while(comp == 0) {
//			if(index.isTerminal() != local.isTerminal() ||
//					index.getLocalIndex() != local.getLocalIndex() ||
//					index.getInstanceIndex() != local.getInstanceIndex()) {
//				if(local.localIndex > index.localIndex) {
//					return 1;
//				} else if(local.localIndex < index.localIndex) {
//					return -1;
//				} else if (local.instanceIndex > index.instanceIndex) {
//					return 1;
//				} else if (local.instanceIndex < index.instanceIndex) {
//					return -1;
//				}
//				
//				//This case is here as a fallback, but it shouldn't really
//				//ever be the case that two references have the same chain
//				//of indices without terminating at the same level.
//				else if (local.isTerminal() && !index.isTerminal()) {
//					return -1;
//				} else {
//					return 1;
//				}
//			}
//			else if(local.isTerminal()) {
//				break;
//			}
//			local = local.getNextLevel();
//			index = index.getNextLevel();
//		}
//		return comp;
	}
	
	/**
	 * @return Only the local component of this Form Index.
	 */
	public FormIndex snip() {
		FormIndex retval = new FormIndex(localIndex, instanceIndex);
		return retval;
	}

	public String toString() {
		String ret = "";
		FormIndex ref = this;
		while (ref != null) {
			ret += ref.getLocalIndex();
			ret += ref.getInstanceIndex()== -1? ", " : "_" + ref.getInstanceIndex() + ", ";
			ref = ref.nextLevel;
		}
		return ret;
	}
	
	/**
	 * Trims any negative indices from the end of the passed in index.
	 * 
	 * @param index
	 * @return
	 */
	public static FormIndex trimNegativeIndices(FormIndex index) {
		if(!index.isTerminal()) {
			return new FormIndex(trimNegativeIndices(index.nextLevel),index);
		} else {
			if(index.getLocalIndex() < 0) { 
				return null;
			} else {
				return index;
			}
		}
	}
	
	public static boolean isSubElement(FormIndex parent, FormIndex child) {
		while(!parent.isTerminal() && !child.isTerminal()) {
			if(parent.getLocalIndex() != child.getLocalIndex()) {
				return false;
			}
			if(parent.getInstanceIndex() != child.getInstanceIndex()) {
				return false;
			}
			parent = parent.nextLevel;
			child = child.nextLevel;
		}
		//If we've gotten this far, at least one of the two is terminal
		if(!parent.isTerminal() && child.isTerminal()) {
			//can't be the parent if the child is earlier on 
			return false;
		}
		else if(parent.getLocalIndex() != child.getLocalIndex()) {
			//Either they're at the same level, in which case only 
			//identical indices should match, or they should have
			//the same root
			return false;
		}
		else if(parent.getInstanceIndex() != -1 && (parent.getInstanceIndex() != child.getInstanceIndex())) {
			return false;
		}
		//Barring all of these cases, it should be true.
		return true;
	}
}
