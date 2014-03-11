/**
 * 
 */
package org.javarosa.core.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

/**
 * Directed A-cyclic (NOT ENFORCED) graph datatype.
 * 
 * Genericized with two types: An unique index value (representing the node) and a generic
 * set of data to associate with that node
 * 
 * @author ctsims
 *
 */
public class DAG<I,N> {
	//TODO: This is a really unsafe datatype. Needs an absurd amount of updating for representation
	//invariance, synchronicity, cycle detection, etc.
	
	Hashtable<I, N> nodes; 
	Hashtable<I,Vector<I>> edge;
	Hashtable<I,Vector<I>> inverse;
	
	public DAG() {
		nodes = new Hashtable<I,N>();
		edge = new Hashtable<I,Vector<I>>();
		inverse = new Hashtable<I,Vector<I>>();
	}
	
	public void addNode(I i, N n) {
		nodes.put(i, n);
	}
	
	/**
	 * Connect Source -> Destination
	 * @param source
	 * @param destination
	 */
	public void setEdge(I source, I destination) {
		addToEdge(edge, source, destination);
		addToEdge(inverse, destination, source);
	}
	
	private void addToEdge(Hashtable<I,Vector<I>> edgeList, I a, I b) {
		Vector<I> edge;
		if(edgeList.containsKey(a)) {
			edge = edgeList.get(a);
		} else {
			edge = new Vector<I>();
		}
		edge.addElement(b);
		edgeList.put(a, edge);
	}
	
	public Vector<I> getParents(I index) {
		if(inverse.containsKey(index)) {
			return inverse.get(index);
		} else {
			return null;
		}
	}
	
	public Vector<I> getChildren(I index) {
		if(!edge.containsKey(index)) {
			return null;
		} else {
			return edge.get(index);
		}
	}
	
	public N getNode(I index) {
		return nodes.get(index);
	}
	
	//Is that the right name?
	/**
	 * @return Indices for all nodes in the graph which are not the target of
	 * any edges in the graph
	 */
	public Stack<I> getSources() {
		Stack<I> roots = new Stack();
		for(Enumeration en = nodes.keys(); en.hasMoreElements() ; ) {
			I i = (I)en.nextElement();
			if(!inverse.containsKey(i)) {
				roots.addElement(i);
			}
		}
		return roots;
	}
}
