package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ElementExistsVisitor;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.PrototypeFactory;


/**
 * QuestionDataElement is a TreeElement of a DataModelTree that is
 * not a leaf, and maintains an arbitrarily sized list of child 
 * TreeElements, none of which are duplicated either in its subtree,
 * or in the tree defined by the node's referenced root.
 * 
 * In an XML Analogy, this represents a non-terminal element with
 * other nested elements.
 * 
 * QuestionDataGroups cannot resolve bindings, or have values set or
 * retrieved from them by default, but can be subclassed in order to
 * implement those methods.
 * 
 * @author Clayton Sims
 *
 */
public class QuestionDataGroup extends TreeElement {
	/** The parent node for this Element **/
	protected QuestionDataGroup parent;
	
	/** List of TreeElements */
	protected Vector children;

	public QuestionDataGroup() {
		//Until a node group is told otherwise, it is its own root.
		this.root = this;
		this.children = new Vector();
	}
	
	/**
	 * Creates a new QuestionDataGroup.
	 * 
	 * @param name The name of this TreeElement
	 */
	public QuestionDataGroup(String name) {
		this();
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
	 * @see org.javarosa.core.model.instance.TreeElement#matchesReference(org.javarosa.core.model.IDataReference)
	 */
	public boolean matchesReference(IDataReference reference) {
		//This class should be subclassed in order to return values from reference
		return false;
	}
	
	public IAnswerData getValue() {
		//This class needs to be subclassed in order to return values
		return null;
	}
	
	public void setName(String name) {
		this.name =name;
	}
	
	public void setReference(IDataReference reference) {
		//This class cannot hold references
	}
	
	public void setValue(IAnswerData data) {
		//This class does not hold data
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
			//System.out.println("Contains on " + element.getName());
				if(element.contains((child))) {
					return true;
				}
		}
		return false;
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
	
	protected void removeChild(TreeElement child) {
		children.removeElement(child);
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
	
	protected void readNodeAttributes(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		name = ExtUtil.readString(in);
	}

	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		readNodeAttributes(in, pf);
		int numChildren = ExtUtil.readInt(in);
		for(int i = 0 ; i < numChildren ; ++i ) {
			boolean group = ExtUtil.readBool(in);
			if(group) {
				QuestionDataGroup newGroup = (QuestionDataGroup)ExtUtil.read(in, new ExtWrapTagged(), pf);
				addChild(newGroup);				
			}
			else {
				QuestionDataElement element = (QuestionDataElement)ExtUtil.read(in, QuestionDataElement.class, pf);
				addChild(element);
			}
		}
	}

	protected void writeNodeAttributes(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, name);
	}
	
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeBool(out, true); //True for groups, false for DataElements
		
		ExtWrapTagged.writeTag(out, this); //ugh!
		//  out.writeUTF(this.getClass().getName());
		
		writeNodeAttributes(out);
		
		ExtUtil.writeNumeric(out, children.size());		
		//This node's children are stored in a depth-first manner by the serializing visitor
	}
}