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
package org.javarosa.core.model.instance;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.condition.Constraint;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.UncastData;
import org.javarosa.core.model.instance.utils.CompactInstanceWrapper;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.model.util.restorable.RestoreUtils;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * An element of a FormInstance.
 * 
 * TreeElements represent an XML node in the instance. It may either have a value (e.g., <name>Drew</name>),
 * a number of TreeElement children (e.g., <meta><device /><timestamp /><user_id /></meta>), or neither (e.g.,
 * <empty_node />)
 * 
 * TreeElements can also represent attributes. Attributes are unique from normal elements in that they are
 * not "children" of their parent, and are always leaf nodes: IE cannot have children.
 * 
 * @author Clayton Sims
 * 
 */

 public class TreeElement implements Externalizable {
	private String name; // can be null only for hidden root node
	public int multiplicity; // see TreeReference for special values
	private TreeElement parent;
	public boolean repeatable;
	public boolean isAttribute;

	private IAnswerData value;
	private Vector children = new Vector();

	/* model properties */
	public int dataType = Constants.DATATYPE_NULL; //TODO
	public boolean required = false;// TODO
	private Constraint constraint = null;
	private String preloadHandler = null;
	private String preloadParams = null;
	private Vector<TreeElement> bindAttributes = new Vector<TreeElement>();

	private boolean relevant = true;
	private boolean enabled = true;
	// inherited properties 
	private boolean relevantInherited = true;
	private boolean enabledInherited = true;

	private Vector observers;

	private Vector<TreeElement> attributes;
	
	private String namespace;
	
	
	/**
	 * TreeElement with null name and 0 multiplicity? (a "hidden root" node?)
	 */
	public TreeElement() {
		this(null, TreeReference.DEFAULT_MUTLIPLICITY);
	}

	public TreeElement(String name) {
		this(name, TreeReference.DEFAULT_MUTLIPLICITY);
	}

	public TreeElement(String name, int multiplicity) {
		this.name = name;
		this.multiplicity = multiplicity;
		this.parent = null;
		attributes = new Vector<TreeElement>(0);
	}
	
	/**
	 * Construct a TreeElement which represents an attribute with the provided 
	 * namespace and name.
	 *  
	 * @param namespace - if null will be converted to empty string
	 * @param name
	 * @param value
	 * @return A new instance of a TreeElement
	 */
	public static TreeElement constructAttributeElement(String namespace, String name, String value) {
		TreeElement element = new TreeElement(name);
		element.isAttribute = true;
		element.namespace = (namespace == null) ? "" : namespace;
		element.multiplicity = TreeReference.INDEX_ATTRIBUTE;
		element.value = new UncastData(value);
		return element;
	}
	
	/**
	 * Retrieves the TreeElement representing the attribute for
	 * the provided namespace and name, or null if none exists.
	 * 
	 * If 'null' is provided for the namespace, it will match the first
	 * attribute with the matching name.
	 * 
	 * @param attributes - list of attributes to search
	 * @param namespace
	 * @param name
	 * @return TreeElement
	 */
	public static TreeElement getAttribute(Vector<TreeElement> attributes, String namespace, String name) {
		for (TreeElement attribute : attributes) {
			if(attribute.getName().equals(name) && (namespace == null || namespace.equals(attribute.namespace))) {
				return attribute;
			}
		}
		return null;
	}

	public static void setAttribute(TreeElement parent, Vector<TreeElement> attrs, String namespace, String name, String value) {

		TreeElement attribut = getAttribute(attrs, namespace, name);
		if ( attribut != null ) {
			if (value == null) {
				attrs.remove(attribut);
			} else {
				attribut.setValue(new UncastData(value));
			}
			return;
		}
		
		// null-valued attributes are a "remove-this" instruction... ignore them
		if ( value == null ) return;
		
		// create an attribute...
		TreeElement attr = TreeElement.constructAttributeElement(namespace, name, value);
		attr.setParent(parent);

		attrs.addElement(attr);
	}
	
	public boolean isLeaf() {
		return (children.size() == 0);
	}

	public boolean isChildable() {
		return (value == null);
	}

	public void setValue(IAnswerData value) {
		if (isLeaf()) {
			this.value = value;
		} else {
			throw new RuntimeException("Can't set data value for node that has children!");
		}
	}

	public TreeElement getChild(String name, int multiplicity) {
		if (name.equals(TreeReference.NAME_WILDCARD)) {
			if(multiplicity == TreeReference.INDEX_TEMPLATE || this.children.size() < multiplicity + 1) {
				return null;
			}
			return (TreeElement) this.children.elementAt(multiplicity); //droos: i'm suspicious of this
		} else {
			for (int i = 0; i < this.children.size(); i++) {
				TreeElement child = (TreeElement) this.children.elementAt(i);
				if (name.equals(child.getName()) && child.getMult() == multiplicity) {
					return child;
				}
			}
		}

		return null;
	}

	/**
	 * 
	 * Get all the child nodes of this element, with specific name
	 * 
	 * @param name
	 * @return
	 */
	public Vector<TreeElement> getChildrenWithName(String name) {
		return getChildrenWithName(name, false);
	}

	private Vector<TreeElement> getChildrenWithName(String name, boolean includeTemplate) {
		Vector<TreeElement> v = new Vector<TreeElement>();

		for (int i = 0; i < this.children.size(); i++) {
			TreeElement child = (TreeElement) this.children.elementAt(i);
			if ((child.getName().equals(name) || name.equals(TreeReference.NAME_WILDCARD))
					&& (includeTemplate || child.multiplicity != TreeReference.INDEX_TEMPLATE))
				v.addElement(child);
		}

		return v;
	}

	public int getNumChildren() {
		return this.children.size();
	}

	public TreeElement getChildAt (int i) {
		return (TreeElement)children.elementAt(i);
	}
	
	/**
	 * Add a child to this element
	 * 
	 * @param child
	 */
	public void addChild(TreeElement child) {
		addChild(child, false);
	}

	private void addChild(TreeElement child, boolean checkDuplicate) {
		if (!isChildable()) {
			throw new RuntimeException("Can't add children to node that has data value!");
		}

		if (child.multiplicity == TreeReference.INDEX_UNBOUND) {
			throw new RuntimeException("Cannot add child with an unbound index!");
		}

		if (checkDuplicate) {
			TreeElement existingChild = getChild(child.name, child.multiplicity);
			if (existingChild != null) {
				throw new RuntimeException("Attempted to add duplicate child!");
			}
		}

		// try to keep things in order
		int i = children.size();
		if (child.getMult() == TreeReference.INDEX_TEMPLATE) {
			TreeElement anchor = getChild(child.getName(), 0);
			if (anchor != null)
				i = children.indexOf(anchor);
		} else {
			TreeElement anchor = getChild(child.getName(),
					(child.getMult() == 0 ? TreeReference.INDEX_TEMPLATE : child.getMult() - 1));
			if (anchor != null)
				i = children.indexOf(anchor) + 1;
		}
		children.insertElementAt(child, i);
		child.setParent(this);
		
		child.setRelevant(isRelevant(), true);
		child.setEnabled(isEnabled(), true);
	}

	public void removeChild(TreeElement child) {
		children.removeElement(child);
	}

	public void removeChild(String name, int multiplicity) {
		TreeElement child = getChild(name, multiplicity);
		if (child != null) {
			removeChild(child);
		}
	}

	public void removeChildren(String name) {
		removeChildren(name, false);
	}

	public void removeChildren(String name, boolean includeTemplate) {
		Vector v = getChildrenWithName(name, includeTemplate);
		for (int i = 0; i < v.size(); i++) {
			removeChild((TreeElement) v.elementAt(i));
		}
	}

	public void removeChildAt(int i) {
		children.removeElementAt(i);

	}

	public int getChildMultiplicity(String name) {
		return getChildrenWithName(name, false).size();
	}

	public TreeElement shallowCopy() {
		TreeElement newNode = new TreeElement(name, multiplicity);
		newNode.parent = parent;
		newNode.repeatable = repeatable;
		newNode.dataType = dataType;
		newNode.relevant = relevant;
		newNode.required = required;
		newNode.enabled = enabled;
		newNode.constraint = constraint;
		newNode.preloadHandler = preloadHandler;
		newNode.preloadParams = preloadParams;
		newNode.bindAttributes = bindAttributes;

		newNode.attributes = new Vector<TreeElement>();
		for (int i = 0; i < attributes.size(); i++) {
			TreeElement attr = (TreeElement) attributes.elementAt(i);
			newNode.setAttribute(attr.getNamespace(), attr.getName(), attr.getAttributeValue());
		}

		if (value != null) {
			newNode.value = value.clone();
		}

		newNode.children = children;
		return newNode;
	}

	public TreeElement deepCopy(boolean includeTemplates) {
		TreeElement newNode = shallowCopy();

		newNode.children = new Vector();
		for (int i = 0; i < children.size(); i++) {
			TreeElement child = (TreeElement) children.elementAt(i);
			if (includeTemplates || child.getMult() != TreeReference.INDEX_TEMPLATE) {
				newNode.addChild(child.deepCopy(includeTemplates));
			}
		}

		return newNode;
	}

	/* ==== MODEL PROPERTIES ==== */

	// factoring inheritance rules
	public boolean isRelevant() {
		return relevantInherited && relevant;
	}

	// factoring in inheritance rules
	public boolean isEnabled() {
		return enabledInherited && enabled;
	}

	/* ==== SPECIAL SETTERS (SETTERS WITH SIDE-EFFECTS) ==== */

	public boolean setAnswer(IAnswerData answer) {
		if (value != null || answer != null) {
			setValue(answer);
			alertStateObservers(FormElementStateListener.CHANGE_DATA);
			return true;
		} else {
			return false;
		}
	}

	public void setRequired(boolean required) {
		if (this.required != required) {
			this.required = required;
			alertStateObservers(FormElementStateListener.CHANGE_REQUIRED);
		}
	}

	public void setRelevant(boolean relevant) {
		setRelevant(relevant, false);
	}

	private void setRelevant(boolean relevant, boolean inherited) {
		boolean oldRelevancy = isRelevant();
		if (inherited) {
			this.relevantInherited = relevant;
		} else {
			this.relevant = relevant;
		}

		if (isRelevant() != oldRelevancy) {
			for (int i = 0; i < children.size(); i++) {
				((TreeElement) children.elementAt(i)).setRelevant(isRelevant(),
						true);
			}
			
			for(int i = 0 ; i < attributes.size(); ++i ) {
				attributes.elementAt(i).setRelevant(isRelevant(), true);
			}
			alertStateObservers(FormElementStateListener.CHANGE_RELEVANT);
		}
	}

	public void setBindAttributes(Vector<TreeElement> bindAttributes ) {
		// create new tree elements for all the bind definitions...
		for ( TreeElement ref : bindAttributes ) {
			setBindAttribute(ref.getNamespace(), ref.getName(), ref.getAttributeValue());
		}
	}
	
	public Vector<TreeElement> getBindAttributes() {
		return bindAttributes;
	}
	
	/**
	 * Retrieves the TreeElement representing an arbitrary bind attribute
	 * for this element at the provided namespace and name, or null if none exists.
	 * 
	 * If 'null' is provided for the namespace, it will match the first
	 * attribute with the matching name.
	 * 
	 * @param index
	 * @return TreeElement
	 */
	public TreeElement getBindAttribute(String namespace, String name) {
		return getAttribute(bindAttributes, namespace, name);
	}

	/**
	 * get value of the bind attribute with namespace:name' in the vector
	 * 
	 * @param index
	 * @return String
	 */
	public String getBindAttributeValue(String namespace, String name) {
		TreeElement element = getBindAttribute(namespace,name);
		return element == null ? null: getAttributeValue(element);
	}
	
	public void setBindAttribute(String namespace, String name, String value) {
		setAttribute(this, bindAttributes, namespace, name, value);
	}
	
	public void setEnabled(boolean enabled) {
		setEnabled(enabled, false);
	}

	public void setEnabled(boolean enabled, boolean inherited) {
		boolean oldEnabled = isEnabled();
		if (inherited) {
			this.enabledInherited = enabled;
		} else {
			this.enabled = enabled;
		}

		if (isEnabled() != oldEnabled) {
			for (int i = 0; i < children.size(); i++) {
				((TreeElement) children.elementAt(i)).setEnabled(isEnabled(),
						true);
			}
			alertStateObservers(FormElementStateListener.CHANGE_ENABLED);
		}
	}

	/* ==== OBSERVER PATTERN ==== */

	public void registerStateObserver(FormElementStateListener qsl) {
		if (observers == null)
			observers = new Vector();

		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}

	public void unregisterStateObserver(FormElementStateListener qsl) {
		if (observers != null) {
			observers.removeElement(qsl);
			if (observers.isEmpty())
				observers = null;
		}
	}

	public void unregisterAll() {
		observers = null;
	}

	public void alertStateObservers(int changeFlags) {
		if (observers != null) {
			for (Enumeration e = observers.elements(); e.hasMoreElements();)
				((FormElementStateListener) e.nextElement())
						.formElementStateChanged(this, changeFlags);
		}
	}

	/* ==== VISITOR PATTERN ==== */

	/**
	 * Visitor pattern acceptance method.
	 * 
	 * @param visitor
	 *            The visitor traveling this tree
	 */
	public void accept(ITreeVisitor visitor) {
		visitor.visit(this);

		Enumeration en = children.elements();
		while (en.hasMoreElements()) {
			((TreeElement) en.nextElement()).accept(visitor);
		}

	}

	/* ==== Attributes ==== */

	/**
	 * Returns the number of attributes of this element.
	 */
	public int getAttributeCount() {
		return attributes.size();
	}

	/**
	 * get namespace of attribute at 'index' in the vector
	 * 
	 * @param index
	 * @return String
	 */
	public String getAttributeNamespace(int index) {
		return attributes.elementAt(index).namespace;
	}

	/**
	 * get name of attribute at 'index' in the vector
	 * 
	 * @param index
	 * @return String
	 */
	public String getAttributeName(int index) {
		return attributes.elementAt(index).name;
	}

	/**
	 * get value of attribute at 'index' in the vector
	 * 
	 * @param index
	 * @return String
	 */
	public String getAttributeValue(int index) {
		return getAttributeValue(attributes.elementAt(index));
	}
	
	/**
	 * Get the String value of the provided attribute 
	 * 
	 * @param attribute
	 * @return
	 */
	private String getAttributeValue(TreeElement attribute) {
		if(attribute.getValue() == null) {
			return null;
		} else {
			return attribute.getValue().uncast().getString();
		}
	}
	
	public String getAttributeValue() {
		if ( !isAttribute ) {
			throw new IllegalStateException("this is not an attribute");
		}
		return getValue().uncast().getString();
	}
	
	/**
	 * Retrieves the TreeElement representing the attribute at
	 * the provided namespace and name, or null if none exists.
	 * 
	 * If 'null' is provided for the namespace, it will match the first
	 * attribute with the matching name.
	 * 
	 * @param index
	 * @return TreeElement
	 */
	public TreeElement getAttribute(String namespace, String name) {
		return getAttribute(attributes, namespace, name);
	}

	/**
	 * get value of attribute with namespace:name' in the vector
	 * 
	 * @param index
	 * @return String
	 */
	public String getAttributeValue(String namespace, String name) {
		TreeElement element = getAttribute(namespace,name);
		return element == null ? null: getAttributeValue(element);
	}

	/**
	 * Sets the given attribute; a value of null removes the attribute
	 * 
	 * */
	public void setAttribute(String namespace, String name, String value) {
		setAttribute(this, attributes, namespace, name, value);
	}
	
	/* ==== SERIALIZATION ==== */

	/*
	 * TODO:
	 * 
	 * this new serialization scheme is kind of lame. ideally, we shouldn't have
	 * to sub-class TreeElement at all; we should have an API that can
	 * seamlessly represent complex data model objects (like weight history or
	 * immunizations) as if they were explicity XML subtrees underneath the
	 * parent TreeElement
	 * 
	 * failing that, we should wrap this scheme in an ExternalizableWrapper
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.services.storage.utilities.Externalizable#readExternal
	 * (java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		name = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		multiplicity = ExtUtil.readInt(in);
		repeatable = ExtUtil.readBool(in);
		value = (IAnswerData) ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);

		// children = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new
		// ExtWrapList(TreeElement.class), pf));

		// Jan 22, 2009 - csims@dimagi.com
		// old line: children = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new
		// ExtWrapList(TreeElement.class), pf));
		// New Child deserialization
		// 1. read null status as boolean
		// 2. read number of children
		// 3. for i < number of children
		// 3.1 if read boolean true , then create TreeElement and deserialize
		// directly.
		// 3.2 if read boolean false then create tagged element and deserialize
		// child
		if (!ExtUtil.readBool(in)) {
			// 1.
			children = null;
		} else {
			children = new Vector();
			// 2.
			int numChildren = (int) ExtUtil.readNumeric(in);
			// 3.
			for (int i = 0; i < numChildren; ++i) {
				boolean normal = ExtUtil.readBool(in);
				TreeElement child;
				
				if (normal) {
					// 3.1
					child = new TreeElement();
					child.readExternal(in, pf);
				} else {
					// 3.2
					child = (TreeElement) ExtUtil.read(in, new ExtWrapTagged(), pf);
				}
				child.setParent(this);
				children.addElement(child);
			}
		}

		// end Jan 22, 2009

		dataType = ExtUtil.readInt(in);
		relevant = ExtUtil.readBool(in);
		required = ExtUtil.readBool(in);
		enabled = ExtUtil.readBool(in);
		relevantInherited = ExtUtil.readBool(in);
		enabledInherited = ExtUtil.readBool(in);
		constraint = (Constraint) ExtUtil.read(in, new ExtWrapNullable(
				Constraint.class), pf);
		preloadHandler = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		preloadParams = ExtUtil.nullIfEmpty(ExtUtil.readString(in));

		bindAttributes = ExtUtil.readAttributes(in, this);
		
		attributes = ExtUtil.readAttributes(in, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.javarosa.core.services.storage.utilities.Externalizable#writeExternal
	 * (java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(name));
		ExtUtil.writeNumeric(out, multiplicity);
		ExtUtil.writeBool(out, repeatable);
		ExtUtil.write(out, new ExtWrapNullable(value == null ? null : new ExtWrapTagged(value)));

		// Jan 22, 2009 - csims@dimagi.com
		// old line: ExtUtil.write(out, new
		// ExtWrapList(ExtUtil.emptyIfNull(children)));
		// New Child serialization
		// 1. write null status as boolean
		// 2. write number of children
		// 3. for all child in children
		// 3.1 if child type == TreeElement write boolean true , then serialize
		// directly.
		// 3.2 if child type != TreeElement, write boolean false, then tagged
		// child
		if (children == null) {
			// 1.
			ExtUtil.writeBool(out, false);
		} else {
			// 1.
			ExtUtil.writeBool(out, true);
			// 2.
			ExtUtil.writeNumeric(out, children.size());
			// 3.
			Enumeration en = children.elements();
			while (en.hasMoreElements()) {
				TreeElement child = (TreeElement) en.nextElement();
				if (child.getClass() == TreeElement.class) {
					// 3.1
					ExtUtil.writeBool(out, true);
					child.writeExternal(out);
				} else {
					// 3.2
					ExtUtil.writeBool(out, false);
					ExtUtil.write(out, new ExtWrapTagged(child));
				}
			}
		}

		// end Jan 22, 2009

		ExtUtil.writeNumeric(out, dataType);
		ExtUtil.writeBool(out, relevant);
		ExtUtil.writeBool(out, required);
		ExtUtil.writeBool(out, enabled);
		ExtUtil.writeBool(out, relevantInherited);
		ExtUtil.writeBool(out, enabledInherited);
		ExtUtil.write(out, new ExtWrapNullable(constraint)); // TODO: inefficient for repeats
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(preloadHandler));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(preloadParams));

		ExtUtil.writeAttributes(out, bindAttributes);
		
		ExtUtil.writeAttributes(out, attributes);
	}

	//rebuilding a node from an imported instance
	//  there's a lot of error checking we could do on the received instance, but it's
	//  easier to just ignore the parts that are incorrect
	public void populate(TreeElement incoming, FormDef f) {
		if (this.isLeaf()) {
			// check that incoming doesn't have children?

			IAnswerData value = incoming.getValue();
			if (value == null) {
				this.setValue(null);
			} else if (this.dataType == Constants.DATATYPE_TEXT
					|| this.dataType == Constants.DATATYPE_NULL) {
				this.setValue(value); // value is a StringData
			} else {
				String textVal = (String) value.getValue();
				IAnswerData typedVal = RestoreUtils.xfFact.parseData(textVal, this.dataType, this.getRef(), f);
				this.setValue(typedVal);
			}
		} else {
			Vector names = new Vector();
			for (int i = 0; i < this.getNumChildren(); i++) {
				TreeElement child = this.getChildAt(i);
				if (!names.contains(child.getName())) {
					names.addElement(child.getName());
				}
			}

			// remove all default repetitions from skeleton data model (_preserving_ templates, though)
			for (int i = 0; i < this.getNumChildren(); i++) {
				TreeElement child = this.getChildAt(i);
				if (child.repeatable && child.getMult() != TreeReference.INDEX_TEMPLATE) {
					this.removeChildAt(i);
					i--;
				}
			}

			// make sure ordering is preserved (needed for compliance with xsd schema)
			if (this.getNumChildren() != names.size()) {
				throw new RuntimeException("sanity check failed");
			}
			
			for (int i = 0; i < this.getNumChildren(); i++) {
				TreeElement child = this.getChildAt(i);
				String expectedName = (String) names.elementAt(i);

				if (!child.getName().equals(expectedName)) {
					TreeElement child2 = null;
					int j;

					for (j = i + 1; j < this.getNumChildren(); j++) {
						child2 = this.getChildAt(j);
						if (child2.getName().equals(expectedName)) {
							break;
						}
					}
					if (j == this.getNumChildren()) {
						throw new RuntimeException("sanity check failed");
					}

					this.removeChildAt(j);
					this.children.insertElementAt(child2, i);
				}
			}
			// java i hate you so much

			for (int i = 0; i < this.getNumChildren(); i++) {
				TreeElement child = this.getChildAt(i);
				Vector newChildren = incoming.getChildrenWithName(child.getName());

				if (child.repeatable) {
				    for (int k = 0; k < newChildren.size(); k++) {
				        TreeElement newChild = child.deepCopy(true);
				        newChild.setMult(k);
				        this.children.insertElementAt(newChild, i + k + 1);
				        newChild.populate((TreeElement)newChildren.elementAt(k), f);
				    }
				    i += newChildren.size();
				} else {

					if (newChildren.size() == 0) {
						child.setRelevant(false);
					} else {
						child.populate((TreeElement)newChildren.elementAt(0), f);
					}
				}
			}
		}
	}
	
	//this method is for copying in the answers to an itemset. the template node of the destination
	//is used for overall structure (including data types), and the itemset source node is used for
	//raw data. note that data may be coerced across types, which may result in type conversion error
	//very similar in structure to populate()
	public void populateTemplate(TreeElement incoming, FormDef f) {
		if (this.isLeaf()) {
			IAnswerData value = incoming.getValue();
			if (value == null) {
				this.setValue(null);
			} else {
				Class classType = CompactInstanceWrapper.classForDataType(this.dataType);
				
				if (classType == null) {
					throw new RuntimeException("data type [" + value.getClass().getName() + "] not supported inside itemset");
				} else if (classType.isAssignableFrom(value.getClass()) &&
							!(value instanceof SelectOneData || value instanceof SelectMultiData)) {
					this.setValue(value);
				} else {
					String textVal = RestoreUtils.xfFact.serializeData(value);
					IAnswerData typedVal = RestoreUtils.xfFact.parseData(textVal, this.dataType, this.getRef(), f);
					this.setValue(typedVal);
				}
			}
		} else {
			for (int i = 0; i < this.getNumChildren(); i++) {
				TreeElement child = this.getChildAt(i);
				Vector newChildren = incoming.getChildrenWithName(child.getName());

				if (child.repeatable) {
				    for (int k = 0; k < newChildren.size(); k++) {
				    	TreeElement template = f.getInstance().getTemplate(child.getRef());
				        TreeElement newChild = template.deepCopy(false);
				        newChild.setMult(k);
				        this.children.insertElementAt(newChild, i + k + 1);
				        newChild.populateTemplate((TreeElement)newChildren.elementAt(k), f);
				    }
				    i += newChildren.size();
				} else {
					child.populateTemplate((TreeElement)newChildren.elementAt(0), f);
				}
			}
		}
	}
	
	//return the tree reference that corresponds to this tree element
	public TreeReference getRef () {
		TreeElement elem = this;
		TreeReference ref = TreeReference.selfRef();
		
		while (elem != null) {
			TreeReference step;
			
			if (elem.name != null) {
				step = TreeReference.selfRef();
				step.add(elem.name, elem.multiplicity);
			} else {
				step = TreeReference.rootRef();
			}
						
			ref = ref.parent(step);
			elem = elem.parent;
		}
		return ref;
	}
	
	public int getDepth () {
		TreeElement elem = this;
		int depth = 0;
		
		while (elem.name != null) {
			depth++;
			elem = elem.parent;
		}
		
		return depth;
	}
	
	public String getPreloadHandler() {
		return preloadHandler;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setPreloadHandler(String preloadHandler) {
		this.preloadHandler = preloadHandler;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public String getPreloadParams() {
		return preloadParams;
	}

	public void setPreloadParams(String preloadParams) {
		this.preloadParams = preloadParams;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public int getMult() {
		return multiplicity;
	}

	public void setMult(int multiplicity) {
		this.multiplicity = multiplicity;
	}

	public void setParent (TreeElement parent) {
		this.parent = parent;
	}
	
	public TreeElement getParent () {
		return parent;
	}
	
	public IAnswerData getValue() {
		return value;
	}

}