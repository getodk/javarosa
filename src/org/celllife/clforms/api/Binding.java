/**
 * 
 */
package org.celllife.clforms.api;

/**
 * 
 */
public class Binding {

	private String id;
	private String nodeset;
	private String type;
	private String relevancy;

	/**
	 * 
	 */
	public Binding() {
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
		return id + " " + nodeset + " " + relevancy + " " + type;
	}
	
	

}
