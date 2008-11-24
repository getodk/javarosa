package org.javarosa.core.model;

import java.util.Vector;

import org.javarosa.core.model.utils.Localizable;
import org.javarosa.core.util.externalizable.Externalizable;

/**
 * An IFormDataElement is an element of the physical interaction for
 * a form, an example of an implementing element would be the definition
 * of a Question. 
 * 
 * @author Drew Roos
 *
 */
public interface IFormElement extends Localizable, Externalizable {
	/**
	 * @return The unique ID of this element
	 */
	int getID ();
	
	/**
	 * @param id The new unique ID of this element
	 */
	void setID (int id);
	
	String getName ();
	void setName (String name);
	
	/**
	 * @return A vector containing any children that this element
	 * might have. Null if the element is not able to have child
	 * elements.
	 */
	Vector getChildren ();
	
	/** 
	 * @param v the children of this element, if it is capable of having
	 * child elements.
	 * @throws IllegalStateException if the element is incapable of
	 * having children.
	 */
	void setChildren (Vector v);
	
	/**
	 * @param fe The child element to be added
	 * @throws IllegalStateException if the element is incapable of
	 * having children.
	 */
	void addChild (IFormElement fe);
	
	/**
	 * @param binding The binding to direct which children should be returned
	 * @param result The vector to be filled with this element's children
	 */
	void getChild(IDataReference binding, Vector result);
}
