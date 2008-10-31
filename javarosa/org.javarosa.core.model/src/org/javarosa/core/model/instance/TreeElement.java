package org.javarosa.core.model.instance;

import java.util.Vector;

import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.utils.ITreeVisitor;
import org.javarosa.core.util.externalizable.Externalizable;
import org.javarosa.core.util.externalizable.PrototypeFactoryDeprecated;

/**
 * An element of a DataModelTree. Contains a name, and a
 * reference to the TreeElement that is the root of the
 * tree that contains this element.
 *
 * @author Clayton Sims
 *
 */

public abstract class TreeElement implements Externalizable {

	/** The root of the tree containing this element */
	protected TreeElement root;

	/** The name of this element */
	protected String name;

	/** The vector of attributes */
    protected Vector attributes;

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
     * Returns the number of attributes of this element. */
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

	public abstract void setName(String name);

	public abstract boolean matchesReference(IDataReference reference);

	public abstract IAnswerData getValue();

	public abstract void setReference(IDataReference reference);

	public abstract void setValue(IAnswerData data);

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
		Vector stringArrays = new Vector();
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

}
