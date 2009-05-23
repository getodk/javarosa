/*
 * Copyright (C) 2009 JavaRosa-Core Project
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
import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.condition.Constraint;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.DeserializationException;
import org.javarosa.core.util.externalizable.ExtUtil;
import org.javarosa.core.util.externalizable.ExtWrapList;
import org.javarosa.core.util.externalizable.ExtWrapNullable;
import org.javarosa.core.util.externalizable.ExtWrapTagged;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactory;

/**
 * An element of a DataModelTree.
 *
 * @author Clayton Sims
 *
 */

public class TreeElement implements Externalizable {
	private String name; //can be null only for hidden root node
	public int multiplicity;
	private Vector attributes;
    
    public boolean repeatable;
  //  public boolean isAttribute;  for when we support xml attributes as data nodes
    
    private IAnswerData value;
    private Vector children;

    /* model properties */
    public int dataType = Constants.DATATYPE_NULL;
	private boolean relevant = true;  
	public boolean required = false;
	private boolean enabled = true;    
	private Constraint constraint = null;
	public String preloadHandler = null;
	public String preloadParams = null;
   
	private boolean relevantInherited = true;
	private boolean enabledInherited = true;
	
	private Vector observers;
	
    public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public TreeElement () {
    	this(null, 0);
    }

    public TreeElement (String name) {
		this(name, 0);
	}
    
	public TreeElement (String name, int multiplicity) {
		this.name = name;
		this.multiplicity = multiplicity;
	}
	
	public boolean isLeaf () {
		return (children == null);
	}

	public boolean isChildable () {
		return (value == null);
	}
	
	public String getName () {
		return name;
	}
	
	public void setName (String name) {
		this.name = name;
	}

	public int getMult() {
		return multiplicity;
	}
	
	public void setMult (int multiplicity) {
		this.multiplicity = multiplicity;
	}
	
	public IAnswerData getValue() {
		return value;
	}

	public void setValue(IAnswerData value) {
		if (isLeaf()) {
			this.value = value;
		} else {
			throw new RuntimeException("Can't set data value for node that has children!");
		}
	}

	//may return null! this vector should not be manipulated outside of this class! (namely, don't delete stuff)
	public Vector getChildren () {
		return children;
	}

	public int getNumChildren () {
		return (children == null ? 0 : children.size());
	}
	
	public TreeElement getChild (String name, int multiplicity) {
		if (children == null) {
			return null;
		} else if(name.equals(TreeReference.NAME_WILDCARD)) {
			return (TreeElement)children.elementAt(multiplicity);
		} else {
			for (int i = 0; i < children.size(); i++) {
				TreeElement child = (TreeElement)children.elementAt(i);
				if (name.equals(child.getName()) && child.getMult() == multiplicity) {
					return child;
				}
			}
			return null;
		}
	}

	public Vector getChild (String name) {
		return getChild(name, false);
	}
	
	public Vector getChild (String name, boolean includeTemplate) {
		Vector v = new Vector();
		
		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				TreeElement child = (TreeElement)children.elementAt(i);
				if ((child.getName().equals(name) || name.equals(TreeReference.NAME_WILDCARD)) && (includeTemplate || child.multiplicity != TreeReference.INDEX_TEMPLATE))
					v.addElement(child);
			}
		}
		
		return v;
	}
	
	public void addChild (TreeElement child) {
		addChild(child, false);
	}
	
	public void addChild(TreeElement child, boolean checkDuplicate) {
		if (children == null) {
			if (isChildable()) {
				children = new Vector();
			} else {
				throw new RuntimeException("Can't add children to node that has data value!");
			}
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

		//try to keep things in order
		int i = children.size();
		if (child.getMult() == TreeReference.INDEX_TEMPLATE) {
			TreeElement anchor = getChild(child.getName(), 0);
			if (anchor != null)
				i = children.indexOf(anchor);
		} else {
			TreeElement anchor = getChild(child.getName(), (child.getMult() == 0 ? TreeReference.INDEX_TEMPLATE : child.getMult() - 1));
			if (anchor != null)
				i = children.indexOf(anchor) + 1;
		}
		children.insertElementAt(child, i);
		
		child.setRelevant(isRelevant(), true);
		child.setEnabled(isEnabled(), true);
	}
	
	public void removeChild(TreeElement child) {
		children.removeElement(child);
		nullChildren();
	}
	
	public void removeChild (String name, int multiplicity) {
		TreeElement child = getChild(name, multiplicity);
		if (child != null) {
			removeChild(child);
		}
	}

	public void removeChildren (String name) {
		removeChildren(name, false);
	}
		
	public void removeChildren (String name, boolean includeTemplate) {
		Vector v = getChild(name, includeTemplate);
		for (int i = 0; i < v.size(); i++) {
			removeChild((TreeElement)v.elementAt(i));
		}
	}
	
	public void removeChildAt (int i) {
		children.removeElementAt(i);
		nullChildren();
	}
	
	private void nullChildren () {
		if (children.size() == 0)
			children = null;
	}
	
	public int getChildMultiplicity (String name) {
		return getChild(name, false).size();
	}
	
	public TreeElement shallowCopy () {
		TreeElement newNode = new TreeElement(name, multiplicity);
		newNode.repeatable = repeatable;
		newNode.dataType = dataType;
		newNode.relevant = relevant;
		newNode.required = required;
		newNode.enabled = enabled;
		newNode.constraint = constraint;
		newNode.preloadHandler = preloadHandler;
		newNode.preloadParams = preloadParams;
		
		newNode.setAttributesFromSingleStringVector(getSingleStringAttributeVector());	
		if(value != null) {
			newNode.value = value.clone();
		}
		
		newNode.children = children;
		return newNode;
	}
	
	public TreeElement deepCopy (boolean includeTemplates) {
		TreeElement newNode = shallowCopy();
		
		newNode.children = null;
		for (int i = 0; i < getNumChildren(); i++) {
			TreeElement child = (TreeElement)children.elementAt(i);
			if (includeTemplates || child.getMult() != TreeReference.INDEX_TEMPLATE) {
				newNode.addChild(child.deepCopy(includeTemplates));
			}
		}
		
		return newNode;
	}
	
	/* ==== MODEL PROPERTIES ==== */
	
	//factoring inheritance rules
	public boolean isRelevant () {
		return relevantInherited && relevant;
	}
	
	//factoring in inheritance rules
	public boolean isEnabled () {
		return enabledInherited && enabled;
	}
	
	/* ==== SPECIAL SETTERS (SETTERS WITH SIDE-EFFECTS) ==== */

	public boolean setAnswer (IAnswerData answer) {
		if (value != null || answer != null) {
			setValue(answer);
			alertStateObservers(FormElementStateListener.CHANGE_DATA);
			return true;
		} else {
			return false;
		}
	}
	
	public void setRequired (boolean required) {
		if (this.required != required) {		
			this.required = required;
	    	alertStateObservers(FormElementStateListener.CHANGE_REQUIRED);
		}
	}

	public void setRelevant (boolean relevant) {
		setRelevant(relevant, false);
	}
	
	public void setRelevant (boolean relevant, boolean inherited) {
		boolean oldRelevancy = isRelevant();
		if (inherited) {
			this.relevantInherited = relevant;
		} else {
			this.relevant = relevant; 
		}
			
		if (isRelevant() != oldRelevancy) {		
			for (int i = 0; i < getNumChildren(); i++) {
				((TreeElement)children.elementAt(i)).setRelevant(isRelevant(), true);
			}
	    	alertStateObservers(FormElementStateListener.CHANGE_RELEVANT);
		}
	}
	
	public void setEnabled (boolean enabled) {
		setEnabled(enabled, false);
	}
	
	public void setEnabled (boolean enabled, boolean inherited) {
		boolean oldEnabled = isEnabled();
		if (inherited) {
			this.enabledInherited = enabled;
		} else {
			this.enabled = enabled; 
		}
			
		if (isEnabled() != oldEnabled) {		
			for (int i = 0; i < getNumChildren(); i++) {
				((TreeElement)children.elementAt(i)).setEnabled(isEnabled(), true);
			}
	    	alertStateObservers(FormElementStateListener.CHANGE_ENABLED);
		}
	}
	
	/* ==== OBSERVER PATTERN ==== */
	
	public void registerStateObserver (FormElementStateListener qsl) {
		if (observers == null)
			observers = new Vector();
		
		if (!observers.contains(qsl)) {
			observers.addElement(qsl);
		}
	}
	
	public void unregisterStateObserver (FormElementStateListener qsl) {
		if (observers != null) {
			observers.removeElement(qsl);
			if (observers.isEmpty())
				observers = null;
		}
	}
	
	public void unregisterAll () {
		observers = null;
	}
	
	public void alertStateObservers (int changeFlags) {
		if (observers != null) {
			for (Enumeration e = observers.elements(); e.hasMoreElements(); )
				((FormElementStateListener)e.nextElement()).formElementStateChanged(this, changeFlags);
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
		if (children != null) {
			Enumeration en = children.elements();
			while (en.hasMoreElements()) {
				((TreeElement) en.nextElement()).accept(visitor);
			}
		}
	}

	/*
	 * ==== HARD-CODED ATTRIBUTES (delete once we support writable attributes)
	 * ====
	 */

	/**
	 * Returns the number of attributes of this element.
	 */
	public int getAttributeCount() {
		return attributes == null ? 0 : attributes.size ();
    }

	/**
	 * get namespace of attribute at 'index' in the vector
	 * @param index
	 * @return String
	 */
	public String getAttributeNamespace (int index) {
		return ((String []) attributes.elementAt (index)) [0];
	}

	/**
	 * get name of attribute at 'index' in the vector
	 * @param index
	 * @return String
	 */
	public String getAttributeName (int index) {
		return ((String []) attributes.elementAt (index)) [1];
	}

	/**
	 * get value of attribute at 'index' in the vector
	 * @param index
	 * @return String
	 */
	public String getAttributeValue (int index) {
		return ((String []) attributes.elementAt (index)) [2];
	}

	/**
	 * get value of attribute with namespace:name' in the vector
	 * @param index
	 * @return String
	 */
	public String getAttributeValue (String namespace, String name) {
		for (int i = 0; i < getAttributeCount (); i++) {
			if (name.equals (getAttributeName (i))
				&& (namespace == null || namespace.equals (getAttributeNamespace(i)))) {
				return getAttributeValue (i);
			}
		}
		return null;
	}

	/**
     * Sets the given attribute; a value of null removes the attribute
     *
     *
     * */
	public void setAttribute (String namespace, String name, String value) {
		if (attributes == null)
			attributes = new Vector ();

		if (namespace == null)
			namespace = "";

        for (int i = attributes.size()-1; i >=0; i--){
            String[] attribut = (String[]) attributes.elementAt(i);
            if (attribut[0].equals(namespace) &&
				attribut[1].equals(name)){

				if (value == null) {
	                attributes.removeElementAt(i);

				}
				else {
					attribut[2] = value;
				}
	            return;
			}
        }

		attributes.addElement
			(new String [] {namespace, name, value});
	}
	
	/**
	 * A method for producing a vector of single strings - from the current
	 * attribute vector of string [] arrays.
	 * @return
	 */
	public Vector getSingleStringAttributeVector(){
		Vector strings = new Vector();
		if (attributes == null)
			return null;
		else{
			for(int i =0; i<this.attributes.size();i++){
				String [] array = (String [])attributes.elementAt(i);
				if (array[0]==null || array[0]=="")
					strings.addElement(new String(array[1]+"="+array[2]));
				else
					strings.addElement(new String(array[0]+":"+array[1]+"="+array[2]));
			}
			return strings;
		}
	}

	/**
	 * Method to repopulate the attribute vector from a vector of singleStrings
	 * @param attStrings
	 */
	public void setAttributesFromSingleStringVector(Vector attStrings){
		//Vector stringArrays = new Vector();
		if (attStrings == null)
			attributes = null;
		else{
			this.attributes = new Vector();
			for(int i =0; i<attStrings.size();i++){
				String att = (String)attStrings.elementAt(i);
				String [] array = new String [3];
				int start = 0;
				// get namespace
				int pos = att.indexOf(":");
				if (pos == -1){
					array[0]=null;
					start = 0;
				}
				else{
					array[0]=att.substring(start, pos);
					start = ++pos;
				}
				// get attribute name
				pos = att.indexOf("=");
				array[1]=att.substring(start,pos);
				start = ++pos;
				array[2]= att.substring(start);
				this.setAttribute(array[0], array[1], array[2]);
			}
		}
	}
	
	/* ==== SERIALIZATION ==== */
	
	/* TODO:
	 * 
	 * this new serialization scheme is kind of lame. ideally, we shouldn't have to sub-class TreeElement at all; we
	 * should have an API that can seamlessly represent complex data model objects (like weight history or immunizations) as
	 * if they were explicity XML subtrees underneath the parent TreeElement
	 * 
	 * failing that, we should wrap this scheme in an ExternalizableWrapper
	 */
	
	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#readExternal(java.io.DataInputStream)
	 */
	public void readExternal(DataInputStream in, PrototypeFactory pf) throws IOException, DeserializationException {
		name = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		multiplicity = ExtUtil.readInt(in);
		repeatable = ExtUtil.readBool(in);
		value = (IAnswerData)ExtUtil.read(in, new ExtWrapNullable(new ExtWrapTagged()), pf);
		
		//children = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new ExtWrapList(TreeElement.class), pf));
		
		//Jan 22, 2009 - csims@dimagi.com
		//old line: children = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new ExtWrapList(TreeElement.class), pf));
		//New Child deserialization
		//1. read null status as boolean
		//2. read number of children
		//3. for i < number of children
		//3.1 if read boolean true , then create TreeElement and deserialize directly.
		//3.2 if read boolean false then create tagged element and deserialize child
		if(!ExtUtil.readBool(in)) {
			//1.
			children = null;
		} else {
			children = new Vector();
			//2.
			int numChildren = (int) ExtUtil.readNumeric(in);
			//3.
			for(int i = 0 ; i < numChildren ; ++i) {
				boolean normal = ExtUtil.readBool(in);
				if(normal) {
					//3.1
					TreeElement child = new TreeElement();
					child.readExternal(in, pf);
					children.addElement(child);
				} else {
					//3.2
					TreeElement child = (TreeElement)ExtUtil.read(in, new ExtWrapTagged(), pf);
					children.addElement(child);
				}
			}
		}
		
		//end Jan 22, 2009

		dataType = ExtUtil.readInt(in);
		relevant = ExtUtil.readBool(in);
		required = ExtUtil.readBool(in);
		enabled = ExtUtil.readBool(in);
		relevantInherited = ExtUtil.readBool(in);
		enabledInherited = ExtUtil.readBool(in);
		constraint = (Constraint)ExtUtil.read(in, new ExtWrapNullable(Constraint.class), pf);
		preloadHandler = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		preloadParams = ExtUtil.nullIfEmpty(ExtUtil.readString(in));
		
		Vector attStrings = ExtUtil.nullIfEmpty((Vector)ExtUtil.read(in, new ExtWrapList(String.class), pf));
		setAttributesFromSingleStringVector(attStrings);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.javarosa.core.services.storage.utilities.Externalizable#writeExternal(java.io.DataOutputStream)
	 */
	public void writeExternal(DataOutputStream out) throws IOException {
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(name));
		ExtUtil.writeNumeric(out, multiplicity);
		ExtUtil.writeBool(out, repeatable);
		ExtUtil.write(out, new ExtWrapNullable(value == null ? null : new ExtWrapTagged(value)));
		
		//Jan 22, 2009 - csims@dimagi.com
		//old line: ExtUtil.write(out, new ExtWrapList(ExtUtil.emptyIfNull(children)));
		//New Child serialization
		//1. write null status as boolean
		//2. write number of children
		//3. for all child in children
		//3.1 if child type == TreeElement write boolean true , then serialize directly.
		//3.2 if child type != TreeElement, write boolean false, then tagged child
		if(children == null) {
			//1.
			ExtUtil.writeBool(out, false);
		} else {
			//1.
			ExtUtil.writeBool(out, true);
			//2.
			ExtUtil.writeNumeric(out, children.size());
			//3.
			Enumeration en = children.elements();
			while(en.hasMoreElements()) {
				TreeElement child  = (TreeElement)en.nextElement();
				if(child.getClass() == TreeElement.class) {
					//3.1
					ExtUtil.writeBool(out, true);
					child.writeExternal(out);
				} else {
					//3.2
					ExtUtil.writeBool(out, false);
					ExtUtil.write(out, new ExtWrapTagged(child));
				}
			}
		}
		
		//end Jan 22, 2009


		ExtUtil.writeNumeric(out, dataType);
		ExtUtil.writeBool(out, relevant);
		ExtUtil.writeBool(out, required);
		ExtUtil.writeBool(out, enabled);
		ExtUtil.writeBool(out, relevantInherited);
		ExtUtil.writeBool(out, enabledInherited);
		ExtUtil.write(out, new ExtWrapNullable(constraint)); //TODO: inefficient for repeats
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(preloadHandler));
		ExtUtil.writeString(out, ExtUtil.emptyIfNull(preloadParams));
		
		Vector attStrings = getSingleStringAttributeVector();
		ExtUtil.write(out, new ExtWrapList(ExtUtil.emptyIfNull(attStrings)));
	}
}