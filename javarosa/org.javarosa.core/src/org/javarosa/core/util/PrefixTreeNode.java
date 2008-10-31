package org.javarosa.core.util;

import java.util.Enumeration;
import java.util.Vector;

public class PrefixTreeNode {
	public String prefix;
	public boolean terminal;
	public Vector children;
	
	public PrefixTreeNode (String prefix) {
		this.prefix = prefix;
		this.terminal = false;
	}
	
	public void decompose (Vector v, String s) {
		String stem = s + prefix;
		
		if (terminal)
			v.addElement(stem);
		
		if (children != null) {
			for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
				((PrefixTreeNode)e.nextElement()).decompose(v, stem);
			}
		}		
	}
	
	public boolean equals (Object o) {
		return (o instanceof PrefixTreeNode ? prefix.equals(((PrefixTreeNode)o).prefix) : false);
	}
	
	public String toString () {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		sb.append(prefix);
		if (terminal)
			sb.append("*");
		if (children != null) {
			for (Enumeration e = children.elements(); e.hasMoreElements(); ) {
				sb.append(((PrefixTreeNode)e.nextElement()).toString());
			}
		}
		sb.append("}");
		return sb.toString();
	}
}
