/**
 * 
 */
package org.javarosa.core.model;

import org.javarosa.core.util.Utilities;


/**
 * 
 */
public class XPathBinding implements IBinding {

	private String id;
	private String nodeset;
	private String type;
	private String relevancy;
	private boolean required = false;

	public String preload;
	public String preloadParams;
	
	private String xpathBinding;
	private String relevantString;
	
	/**
	 * 
	 */
	public XPathBinding() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return this.id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNodeset() {
		return this.nodeset;
	}
	public void setNodeset(String nodeset) {
		this.nodeset = nodeset;
	}
	public String getType() {
		return this.type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRelevancy() {
		return this.relevancy;
	}
	public void setRelevancy(String relevancy) {
		this.relevancy = relevancy;
	}

	public String toString() {
		return "id:"+id + " nodset: " + nodeset + " rel: " + relevancy + " req: "+required+" type " + type;
	}

	public void setRequired(String attributeValue) throws Exception {
		System.out.println("setting bind "+this.id+ " req="+attributeValue);
		this.required = Utilities.getBoolean(attributeValue);
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isRequired() {
		return required;
	}
	
	

}
