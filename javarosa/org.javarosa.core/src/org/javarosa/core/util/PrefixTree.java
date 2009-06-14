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

import java.util.Enumeration;
import java.util.Vector;

public class PrefixTree {
	private PrefixTreeNode root;

	public PrefixTree () {
		root = new PrefixTreeNode("");
	}
	
	public static int sharedPrefixLength (String a, String b) {
		int len;
		
		for (len = 0; len < a.length() && len < b.length(); len++) {
			if (a.charAt(len) != b.charAt(len))
				break;
		}
		
		return len;
	}
	
	public void addString (String s) {
		PrefixTreeNode current = root;

		while (s.length() > 0) {
			int len = 0;
			PrefixTreeNode node = null;
			
			if (current.children != null) {
				for (Enumeration e = current.children.elements(); e.hasMoreElements(); ) {
					node = (PrefixTreeNode)e.nextElement();
					len = sharedPrefixLength(s, node.prefix);
					if (len > 0)
						break;
					node = null;
				}
			}
				
			if (node == null) {
				node = new PrefixTreeNode(s);
				len = s.length();
				
				if (current.children == null)
					current.children = new Vector();
				current.children.addElement(node);
			} else if (len < node.prefix.length()) {
				String prefix = s.substring(0, len);
				PrefixTreeNode interimNode = new PrefixTreeNode(prefix);
				
				current.children.removeElement(node);
				node.prefix = node.prefix.substring(len);
				
				current.children.addElement(interimNode);
				interimNode.children = new Vector();
				interimNode.children.addElement(node);
				
				node = interimNode;
			}
			
			current = node;
			s = s.substring(len);
		}
		
		current.terminal = true;
	}
	
	public Vector getStrings () {
		Vector v = new Vector();
		root.decompose(v, "");
		return v;
	}
	
	public String toString() {
		return root.toString();
	}
}
