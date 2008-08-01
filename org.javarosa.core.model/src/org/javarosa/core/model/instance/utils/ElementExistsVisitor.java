package org.javarosa.core.model.instance.utils;

import org.javarosa.core.model.IFormDataModel;
import org.javarosa.core.model.instance.DataModelTree;
import org.javarosa.core.model.instance.QuestionDataElement;
import org.javarosa.core.model.instance.QuestionDataGroup;
import org.javarosa.core.model.instance.TreeElement;

/**
 * The ElementExistsVisitor is responsible for identifying whether any of the nodes 
 * in a given DataModelTree exist in a reference tree. 
 * 
 * @author Clayton Sims
 *
 */
public class ElementExistsVisitor implements ITreeVisitor {

	/** Whether or not a match was found */
	private boolean contains; 
	
	/** The root node defining the reference tree which will be checked against */ 
	private TreeElement rootNode;
	
	/**
	 * Creates a new visitor that is responsible for identifying
	 * whether elements exist inside of the tree defined by the 
	 * root given to this constructor.
	 * 
	 * @param rootNode The root of the tree for in elements
	 * will be searched.
	 */
	public ElementExistsVisitor(TreeElement rootNode) {
		this.rootNode = rootNode;
		contains = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.QuestionDataElement)
	 */
	public void visit(QuestionDataElement element) {
		if(rootNode.contains(element)) {
			contains = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.QuestionDataGroup)
	 */
	public void visit(QuestionDataGroup element) {
		//We can skip the recursive check if the list of children contains the element
		if(rootNode.contains(element)) {
			contains = true;
		}
	}
	
	
	public void visit(DataModelTree tree) {
		// Nothing really
	}
	

	public void visit(IFormDataModel dataModel) {
		if(dataModel.getClass() == DataModelTree.class) {
			((DataModelTree)dataModel).accept(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.javarosa.core.model.utils.ITreeVisitor#visit(org.javarosa.core.model.TreeElement)
	 */
	//TODO: this seems extremely hacky
	//TODO: make this visitor actually work (even with hack); when commented out, form entry seems to work fine
	public void visit(TreeElement element) {
//		if (element instanceof QuestionDataElement) {
//			visit((QuestionDataElement)element);
//		} else if (element instanceof QuestionDataGroup) {
//			visit((QuestionDataGroup)element);
//		} else {
//			throw new RuntimeException("ElementExistsVisitor: unrecognized sub-class of TreeElement. Our hack has come back to haunt us...");
//		}
	}
	
	/**
	 * The return condition of the visitor. Reports whether or not the tree passed
	 * to this visitor contained nodes that were present in the visitor's reference
	 * tree.
	 * 
	 * @return true if any of the elements in the trees visited are present in the
	 * visitor's reference tree. False otherwise.
	 */
	public boolean containsAnyElements() {
		return contains;
	}
}
