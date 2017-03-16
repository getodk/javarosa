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

public class PrefixTreeNode {
	private char[] prefix;
	private boolean terminal;
	private List<PrefixTreeNode> children;
	private PrefixTreeNode parent;

	public PrefixTreeNode (char[] prefix) {
		this.prefix = prefix;
		this.terminal = false;
	}

	public void decompose (List<String> v, String s) {
		String stem = s + new String(prefix);

		if (terminal) {
			v.add(stem);
		}

		if (children != null) {
         for (PrefixTreeNode child : children) {
            child.decompose(v, stem);
         }
		}
	}

	public char[] getPrefix() {
		return prefix;
	}

	public List<PrefixTreeNode> getChildren() {
		return children;
	}

	public boolean equals (Object o) {
		//uh... is this right?
		return (o instanceof PrefixTreeNode ? prefix == ((PrefixTreeNode)o).prefix || ArrayUtilities.arraysEqual(prefix,0, ((PrefixTreeNode)o).prefix, 0) : false);
	}

	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append(prefix);
		if (terminal)
			sb.append("*");
		if (children != null) {
         for (PrefixTreeNode child : children) {
            sb.append(child.toString());
         }
		}
		sb.append("}");
		return sb.toString();
	}

	public String render() {
		StringBuffer temp = new StringBuffer();
		return render(temp);
	}

	public String render(StringBuffer buffer) {
		if(parent != null){
			parent.render(buffer);
		}
		buffer.append(this.prefix);
		return buffer.toString();
	}

	public void seal() {
		if (children != null) {
         for (PrefixTreeNode child : children) {
            child.seal();
         }
		}
		this.children = null;
	}

	public void addChild(PrefixTreeNode node) {
		if(children == null) {
			children = new ArrayList<PrefixTreeNode>(1);
		}
		children.add(node);
		node.parent = this;
	}

	public void setTerminal() {
		//This node is now terminal (we can use this fact to clean things up)
		terminal = true;
	}

	public PrefixTreeNode budChild(PrefixTreeNode node, char[] subPrefix, int subPrefixLen) {
		//make a new child for the subprefix
		PrefixTreeNode newChild = new PrefixTreeNode(subPrefix);

		//remove the child from our tree (we'll re-add it later)
		this.children.remove(node);
		node.parent = null;

		//cut out the middle part of the prefix (which is now this node's domain)
		char[] old = node.prefix;
		node.prefix = new char[old.length - subPrefixLen];
		for(int i = 0 ; i < old.length - subPrefixLen; ++i) {
			node.prefix[i] = old[subPrefixLen + i];
		}

		//replace the old child with the new one, and put it in the proper order
		this.addChild(newChild);
		newChild.addChild(node);

		return newChild;
	}
}
