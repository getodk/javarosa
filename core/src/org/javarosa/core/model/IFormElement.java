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

package org.javarosa.core.model;

import java.util.List;

import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.locale.Localizable;
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
	
	/**
	 * get the TextID for this element used for localization purposes
	 * @return the TextID (bare, no ;form appended to it!!)
	 */
	String getTextID();
	
	/**
	 * Set the textID for this element for use with localization.
	 * @param id the plain TextID WITHOUT any form specification (e.g. ;long)
	 */
	void setTextID(String id);
	
	
	/**
	 * @return A List containing any children that this element
	 * might have. Null if the element is not able to have child
	 * elements.
	 */
   List<IFormElement> getChildren ();
	
	/** 
	 * @param v the children of this element, if it is capable of having
	 * child elements.
	 * @throws IllegalStateException if the element is incapable of
	 * having children.
	 */
	void setChildren (List<IFormElement> v);
	
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

	/**
	 * This method returns the regular
	 * innertext betweem label tags (if present) (&ltlabel&gtinnertext&lt/label&gt).
	 * @return &ltlabel&gt innertext or null (if innertext is not present).
	 */
	public String getLabelInnerText();
	
	
	/**
	 * @return
	 */
	public String getAppearanceAttr();
	
	public void setAppearanceAttr (String appearanceAttr);
	
	/**
	 * Capture additional attributes on a Question or Group
	 * 
	 * @param namespace
	 * @param name
	 * @param value
	 */
	public void setAdditionalAttribute(String namespace, String name, String value);

	/**
	 * Retrieve the value of an additional attribute on a Question or Group
	 * @param namespace
	 * @param name
	 * @return
	 */
	public String getAdditionalAttribute(String namespace, String name);
	
	/**
	 * Retrieve all additional attributes on a Question or Group
	 * 
	 * @return
	 */
	public List<TreeElement> getAdditionalAttributes();

}
