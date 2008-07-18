package org.javarosa.core.model.instance;

import org.javarosa.core.model.DataReferenceFactory;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.services.storage.utilities.Externalizable;

/**
 * An element of a DataModelTree. Contains a name, and a 
 * reference to the TreeElement that is the root of the
 * tree that contains this element. 
 * 
 * @author Clayton Sims
 *
 */
public abstract class TreeElement implements Externalizable {
	
	/** A set of prototype references maintained be the root node to used in deserialization. */
	DataReferenceFactory factory;
	
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
	
	/**
	 * @return the factory
	 */
	public DataReferenceFactory getFactory() {
		if(this == this.getRoot()) {
			return factory;
		}
		else {
			return this.getRoot().getFactory();
		}
	}

	/**
	 * @param factory the factory to set
	 */
	public void setFactory(DataReferenceFactory factory) {
		if(this == this.getRoot()) {
			this.factory = factory;
		}
		else {
			this.getRoot().setFactory(factory);
		}
	}
}
