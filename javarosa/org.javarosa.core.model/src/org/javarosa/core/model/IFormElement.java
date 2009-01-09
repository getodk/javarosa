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
	
	String getTitle ();
	void setTitle (String name);
	
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
	
	IFormElement getChild (int i);
	
	/**
	 * @return A recursive count of how many elements are ancestors of this element.
	 */
	int getDeepChildCount();
	
	/**
	 * @return The data reference for this element
	 */
	IDataReference getBind();
	
	/**
	 * Registers a state observer for this element.
	 * 
	 * @param qsl
	 */
	public void registerStateObserver (FormElementStateListener qsl);
	
	/**
	 * Unregisters a state observer for this element.
	 * 
	 * @param qsl
	 */
	public void unregisterStateObserver (FormElementStateListener qsl);
}
