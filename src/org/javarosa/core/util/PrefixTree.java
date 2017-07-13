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
import java.util.Enumeration;
import java.util.List;

public class PrefixTree {
	//Sometimes the string optimizations here are basically useless
	//due to wide availability of memory. It's easier in many cases
	//to simply keep using the framework, but just disable the actual
	//stemming/prefix ops
	boolean disablePrefixing = false;

	private PrefixTreeNode root;

	int minimumPrefixLength;
	int minimumHeuristicLength;

	//Common delimeters which we'd prefer as prefix breaks rather than
	//maximum string space
	private static final char[] delimiters = {'\\', '/', '.'};
	private static final int delSacrifice = 3;
	boolean finalized = false;

	public PrefixTree () {
		this(0);
	}

	public PrefixTree (int minimumPrefixLength) {
		root = new PrefixTreeNode(new char[0]);
		this.minimumPrefixLength = Math.max(minimumPrefixLength++, 0);
		this.minimumHeuristicLength = Math.max((int)(minimumPrefixLength / 2), 3);
	}

	public static int sharedPrefixLength (char[] a, int aStart, char[] b) {
		int len;
		int minLength = Math.min(a.length - aStart, b.length);

		for (len = 0; len < minLength;len++) {
			if (a[len + aStart] != b[len])
				break;
		}

		return len;
	}

	public PrefixTreeNode addString (String newString) {
		if(finalized) {
			throw new RuntimeException("Can't manipulate a finalized Prefix Tree");
		}

		if(disablePrefixing) {
			PrefixTreeNode newNode = new PrefixTreeNode(newString.toCharArray());
			newNode.setTerminal();
			root.addChild(newNode);
			return newNode;
		}

		PrefixTreeNode current = root;

		char[] chars = newString.toCharArray();
		int currentIndex = 0;

		while (currentIndex < chars.length) {

			//The length of the string we've incorporated into the tree
			int len = 0;

			//The (potential) next node in the tree which prefixes the rest of the string
			PrefixTreeNode node = null;

			//TODO: This would be way faster if we sorted upon insertion....
			if (current.getChildren() != null) {
            for (PrefixTreeNode prefixTreeNode : current.getChildren()) {
               node = prefixTreeNode;

					char[] prefix = node.getPrefix();
					//if(prefix.equals(s)) {
					if(ArrayUtilities.arraysEqual(prefix, 0, chars, currentIndex)) {
						return node;
					}

					len = sharedPrefixLength(chars, currentIndex, prefix);
					if (len > minimumPrefixLength) {
						//See if we have any breaks which might make more heuristic sense than simply grabbing the biggest
						//difference
						for(char c : delimiters) {
							int sepLen = -1;
							for(int i = currentIndex + len -1; i >= currentIndex; i--) {
								if(chars[i] == c) {
									sepLen = i - currentIndex;
									break;
								}
							}
							if(sepLen != -1 && len - sepLen < delSacrifice && sepLen > minimumHeuristicLength) {
								len = sepLen;
								break;
							}
						}

						break;
					}
					node = null;
				}
			}

			//If we didn't find anything that shared any common roots
			if (node == null) {
				//Create a placeholder for the rest of the string
				char[] newArray;
				if(currentIndex == 0) {
					newArray = chars;
				} else {
					newArray = new char[chars.length - currentIndex];
					for(int i = 0 ; i < chars.length - currentIndex; ++i) { newArray[i] = chars[i + currentIndex];}
				}
				node = new PrefixTreeNode(newArray);

				len = chars.length - currentIndex;

				//Add this to the highest level prefix we've found
				current.addChild(node);
			}
			//Otherwise check to see if we are going to split the current prefix
			else if (len < node.getPrefix().length) {
				char[] newPrefix = new char[len];
				for(int i = 0; i < len ; ++i) {
					newPrefix[i] = chars[currentIndex + i];
				}

				PrefixTreeNode interimNode = current.budChild(node, newPrefix, len);

				node = interimNode;
			}

			current = node;
			currentIndex = currentIndex + len;
		}

		current.setTerminal();
		return current;
	}

	public List<String> getStrings () {
		if(finalized) {
			throw new RuntimeException("Can't get the strings from a finalized Prefix Tree");
		}
      List<String> v = new ArrayList<String>(1);
		root.decompose(v, "");
		return v;
	}

	public String toString() {
		return root.toString();
	}
	public void seal() {
		//System.out.println(toString());
		root.seal();
		finalized = true;
	}

	public void clear() {
		finalized = false;
		root = new PrefixTreeNode(new char[0]);
	}
}
