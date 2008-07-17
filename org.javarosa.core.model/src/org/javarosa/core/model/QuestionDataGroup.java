package org.javarosa.core.model;

import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.utils.ElementExistsVisitor;
import org.javarosa.core.model.utils.ITreeVisitor;


/**
 * QuestionDataElement is a TreeElement of a DataModelTree that is
 * not a leaf, and maintains an arbitrarily sized list of child 
 * TreeElements, none of which are duplicated either in its subtree,
 * or in the tree defined by the node's referenced root.
 * 
 * In an XML Analogy, this represents a non-terminal element with
 * other nested elements.
 * 
 * @author Clayton Sims
 *
 */
public class QuestionDataGroup extends TreeElement {
	
	/** The parent node for this Element **/
	QuestionDataGroup parent;
	
	/** List of TreeElements */
	Vector children;
	
	/**
	 * Creates a new QuestionDataGroup.
	 * 
	 * @param name The name of this TreeElement
	 */
	public QuestionDataGroup(String name) {
		this.name = name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#isLeaf()
	 */
	public boolean isLeaf() {
		return this.children.isEmpty();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return A list of TreeElement that are this Element's children
	 */
	public Vector getChildren() {
		return children;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#contains(org.javarosa.core.model.TreeElement)
	 */
	public boolean contains(TreeElement child) {
		if(children.contains(child)) {
			return true;
		}
		Enumeration en = children.elements();
		while(en.hasMoreElements()) {
			TreeElement element = (TreeElement)en.nextElement();
				if(element.contains((child))) {
					return true;
				}
		}
		return true;
	}
	
	/**
	 * Adds the given TreeElement as a child to this element if possible.
	 * If any elements present in the subtree defined by the child node
	 * are also present in the tree defined by this node's reference to 
	 * the tree's root, no additions will occur.
	 * 
	 * @param child The child element, possibly containing its own subtree,
	 * to be added as a child of this node.
	 * @return if this.root.contains( node where child.contains(node) ), no
	 * changes are made, and false is returned. True otherwise.
	 */
	public boolean addChild(TreeElement child) {
		ElementExistsVisitor eevisitor = new ElementExistsVisitor((QuestionDataGroup)this.getRoot());
		eevisitor.visit(child);
		if(eevisitor.containsAnyElements()) {
			return false;
		} else {
			children.addElement(child);
			child.setRoot(this.getRoot());
			return true;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.TreeElement#setRoot(org.javarosa.core.model.TreeElement)
	 */
	protected void setRoot(TreeElement root) {
		this.root = root;
		
		//Recursively set the root of this node's children.
		Enumeration en = children.elements();
		while(en.hasMoreElements()) {
			TreeElement child = (TreeElement)en.nextElement();
			child.setRoot(root);
		}
	}
	
	/**
	 * Visitor pattern acceptance method.
	 * @param visitor The visitor traveling this tree
	 */
	public void accept(ITreeVisitor visitor) {
		visitor.visit(this);
		Enumeration en = children.elements();
		while(en.hasMoreElements()) {
			((TreeElement)en.nextElement()).accept(visitor);
		}
	}
}
