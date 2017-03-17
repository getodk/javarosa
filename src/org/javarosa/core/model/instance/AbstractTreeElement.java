package org.javarosa.core.model.instance;

import java.util.List;

import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.xpath.expr.XPathExpression;

public interface AbstractTreeElement<T extends AbstractTreeElement> {

	public abstract boolean isLeaf();

	public abstract boolean isChildable();

	public abstract String getInstanceName();

	public abstract T getChild(String name, int multiplicity);

	/**
	 * 
	 * Get all the child nodes of this element, with specific name
	 * 
	 * @param name
	 * @return
	 */
	public abstract List<T> getChildrenWithName(String name);
	
	public abstract boolean hasChildren();

	public abstract int getNumChildren();

	public abstract T getChildAt(int i);

	public abstract boolean isRepeatable();

	public abstract boolean isAttribute();
			
	public abstract int getChildMultiplicity(String name);

	/**
	 * Visitor pattern acceptance method.
	 * 
	 * @param visitor
	 *            The visitor traveling this tree
	 */
	public abstract void accept(ITreeVisitor visitor);

	/**
	 * Returns the number of attributes of this element.
	 */
	public abstract int getAttributeCount();

	/**
	 * get namespace of attribute at 'index' in the list
	 * 
	 * @param index
	 * @return String
	 */
	public abstract String getAttributeNamespace(int index);

	/**
	 * get name of attribute at 'index' in the list
	 * 
	 * @param index
	 * @return String
	 */
	public abstract String getAttributeName(int index);

	/**
	 * get value of attribute at 'index' in the list
	 * 
	 * @param index
	 * @return String
	 */
	public abstract String getAttributeValue(int index);

	/**
	 * Retrieves the TreeElement representing the attribute at
	 * the provided namespace and name, or null if none exists.
	 * 
	 * If 'null' is provided for the namespace, it will match the first
	 * attribute with the matching name.
	 * 
	 * @return TreeElement
	 */
	public abstract T getAttribute(String namespace, String name);

	/**
	 * get value of attribute with namespace:name' in the list
	 * 
	 * @return String
	 */
	public abstract String getAttributeValue(String namespace, String name);

	//return the tree reference that corresponds to this tree element
	public abstract TreeReference getRef();

	public abstract int getDepth();

	public abstract String getName();

	public abstract int getMult();

	//Support? 
	public abstract AbstractTreeElement getParent();

	public abstract IAnswerData getValue();

	public abstract int getDataType();
	
	public abstract void clearCaches();

	public abstract boolean isRelevant();
	
	public abstract String getNamespace();
	
	/**
	 * TODO: Worst method name ever. Don't use this unless you know what's up.
	 * 
	 * @param name
	 * @param mult 
	 * @param predicates possibly list of predicates to be evaluated. predicates will be removed from list if they are 
	 * able to be evaluated
	 * @param evalContext
	 * @return
	 */
	public abstract List<TreeReference> tryBatchChildFetch(String name, int mult, List<XPathExpression> predicates, EvaluationContext evalContext);
}