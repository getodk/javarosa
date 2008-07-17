package org.javarosa.core.model;

import org.javarosa.core.model.utils.ITreeVisitor;

/**
 * An element of a DataModelTree. Contains a name, and a 
 * reference to the TreeElement that is the root of the
 * tree that contains this element. 
 * 
 * @author Clayton Sims
 *
 */
public abstract class TreeElement {
	
	/** The root of the tree containing this element */
	protected TreeElement root;
	
	/** The name of this element */
	protected String name;
	
	/**
	 * @return True if the element can contain subelements. False otherwise
	 */
	public abstract boolean isLeaf();
	
	/**
	 * @return The name of this element
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return The root of the tree containing this element
	 */
	public TreeElement getRoot() {
		return root;
	}
	
	/**
	 * Sets the root of this element. Implementing classes
	 * are responsible for identifying that the given root
	 * contains this element
	 * 
	 * @param root The new root of the tree containing
	 * this element
	 */
	protected abstract void setRoot(TreeElement root);
	
	/**
	 * @param element The element to be checked for
	 * @return true if this element is, or any subtree defined
	 * by this element contains, the element provided. false
	 * otherwise.
	 * 
	 */
	public abstract boolean contains(TreeElement element);
	
	/**
	 * Visitor pattern acceptance method.
	 * @param visitor The visitor traveling this tree
	 */
	public void accept(ITreeVisitor visitor) {
		visitor.visit(this);
	}
}
